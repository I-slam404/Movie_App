package com.islam404.movieapp.data.repository

import com.islam404.movieapp.data.mapper.*
import com.islam404.movieapp.data.remote.api.TmdbApi
import com.islam404.movieapp.domain.model.Movie
import com.islam404.movieapp.domain.model.MovieDetail
import com.islam404.movieapp.domain.repository.MovieRepository
import com.islam404.movieapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import com.islam404.movieapp.data.cache.MovieCacheManager
import com.islam404.movieapp.util.AppLogger

class MovieRepositoryImpl @Inject constructor(
    private val api: TmdbApi,
    private val cacheManager: MovieCacheManager
) : MovieRepository {

    /**
     * Cache-First Strategy with Background Refresh
     * 1. Check cache and emit immediately if exists
     * 2. Always fetch fresh data from API (unless cache is very fresh)
     * 3. Update cache and emit fresh data
     */
    private fun cacheFirstThenNetwork(
        category: String,
        page: Int,
        forceRefresh: Boolean,
        fetchFromApi: suspend () -> Pair<List<Movie>, Int> // Returns (movies, totalPages)
    ): Flow<Resource<List<Movie>>> = flow {
        AppLogger.d("MovieRepository", "=== [$category:$page] START (force=$forceRefresh) ===")

        // Step 1: Check cache first
        val cacheResult = if (!forceRefresh) {
            cacheManager.getCachedMovies(category, page)
        } else {
            null
        }

        // Step 2: If we have cache, emit it immediately
        if (cacheResult != null) {
            AppLogger.d("MovieRepository", "[$category:$page] ✓ Cache HIT - emitting ${cacheResult.movies.size} movies (stale=${cacheResult.isStale})")
            emit(Resource.Success(cacheResult.movies))

            // If cache is fresh (not stale), we might skip API call
            if (!cacheResult.isStale && !forceRefresh) {
                AppLogger.d("MovieRepository", "[$category:$page] ✓ Cache is FRESH - skipping API call")
                return@flow
            }

            // Cache is stale, show loading with cached data visible
            AppLogger.d("MovieRepository", "[$category:$page] ⟳ Cache is STALE - fetching fresh data...")
            emit(Resource.Loading(cacheResult.movies))
        } else {
            // No cache, show loading without data
            AppLogger.d("MovieRepository", "[$category:$page] ✗ Cache MISS - showing loading")
            emit(Resource.Loading())
        }

        // Step 3: Fetch fresh data from API
        try {
            AppLogger.d("MovieRepository", "[$category:$page] → Calling API...")
            val (freshMovies, totalPages) = fetchFromApi()
            val hasMore = page < totalPages && freshMovies.isNotEmpty()

            AppLogger.d("MovieRepository", "[$category:$page] ✓ API SUCCESS: ${freshMovies.size} movies (hasMore=$hasMore, totalPages=$totalPages)")

            // Step 4: Check if data actually changed
            val dataChanged = cacheManager.hasDataChanged(category, page, freshMovies)
            AppLogger.d("MovieRepository", "[$category:$page] Data changed: $dataChanged")

            // Step 5: Always update cache with fresh data
            cacheManager.cacheMovies(
                category = category,
                page = page,
                movies = freshMovies,
                totalPages = totalPages,
                hasMore = hasMore
            )

            // Step 6: Always emit fresh data (the ViewModel will decide if UI update is needed)
            AppLogger.d("MovieRepository", "[$category:$page] ✓ Emitting fresh data")
            emit(Resource.Success(freshMovies))

        } catch (e: HttpException) {
            AppLogger.e("MovieRepository", "[$category:$page] ✗ HTTP Error: ${e.code()} - ${e.message()}")
            emit(
                Resource.Error(
                    message = "Server error: ${e.code()}",
                    data = cacheResult?.movies
                )
            )
        } catch (e: IOException) {
            AppLogger.e("MovieRepository", "[$category:$page] ✗ Network Error: ${e.message}")
            emit(
                Resource.Error(
                    message = "No internet connection",
                    data = cacheResult?.movies
                )
            )
        } catch (e: Exception) {
            AppLogger.e("MovieRepository", "[$category:$page] ✗ Unknown Error: ${e.message}")
            emit(
                Resource.Error(
                    message = e.localizedMessage ?: "Unknown error",
                    data = cacheResult?.movies
                )
            )
        }

        AppLogger.d("MovieRepository", "=== [$category:$page] END ===")
    }

    override fun getPopularMovies(page: Int, forceRefresh: Boolean): Flow<Resource<List<Movie>>> =
        cacheFirstThenNetwork(
            category = "popular",
            page = page,
            forceRefresh = forceRefresh,
            fetchFromApi = {
                val response = api.getPopularMovies(page = page)
                Pair(response.results.map { it.toMovie() }, response.totalPages)
            }
        )

    override fun getTopRatedMovies(page: Int, forceRefresh: Boolean): Flow<Resource<List<Movie>>> =
        cacheFirstThenNetwork(
            category = "top_rated",
            page = page,
            forceRefresh = forceRefresh,
            fetchFromApi = {
                val response = api.getTopRatedMovies(page = page)
                Pair(response.results.map { it.toMovie() }, response.totalPages)
            }
        )

    override fun getNowPlayingMovies(page: Int, forceRefresh: Boolean): Flow<Resource<List<Movie>>> =
        cacheFirstThenNetwork(
            category = "now_playing",
            page = page,
            forceRefresh = forceRefresh,
            fetchFromApi = {
                val response = api.getNowPlayingMovies(page = page)
                Pair(response.results.map { it.toMovie() }, response.totalPages)
            }
        )

    override fun getMovieDetail(movieId: Int): Flow<Resource<MovieDetail>> = flow {
        emit(Resource.Loading())

        try {
            val movieDetail = api.getMovieDetail(movieId)
            val credits = api.getMovieCredits(movieId)
            val cast = credits.cast.take(10).map { it.toCast() }

            emit(Resource.Success(movieDetail.toMovieDetail(cast)))
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    override fun searchMovies(query: String, page: Int): Flow<Resource<List<Movie>>> = flow {
        emit(Resource.Loading())

        try {
            val response = api.searchMovies(query, page)
            val movies = response.results.map { it.toMovie() }
            emit(Resource.Success(movies))
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    override suspend fun clearCache() {
        cacheManager.clearCache()
    }
}