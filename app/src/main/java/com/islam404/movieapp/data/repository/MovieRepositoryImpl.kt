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
import android.util.Log

class MovieRepositoryImpl @Inject constructor(
    private val api: TmdbApi,
    private val cacheManager: MovieCacheManager
) : MovieRepository {

    private fun cachedThenFetchMovies(
        category: String,
        page: Int,
        forceRefresh: Boolean,
        fetchFromApi: suspend () -> List<Movie>
    ): Flow<Resource<List<Movie>>> = flow {
        val cachedMovies = cacheManager.getCachedMovies(category, page)

        // 1) Emit cache immediately if present
        if (cachedMovies != null) {
            emit(Resource.Success(cachedMovies))
            Log.d("MovieRepository", "Loaded from cache: $category page $page")
        }

        // 2) Fetch strategy: always revalidate in background.
        //    - If `forceRefresh` is true, we *must* hit network.
        //    - If `forceRefresh` is false, we still fetch to update UI when new movies arrive.
        val shouldFetch = true
        if (!shouldFetch) return@flow

        // 3) Indicate refresh (keep cached visible if any)
        emit(Resource.Loading(cachedMovies))

        try {
            val freshMovies = fetchFromApi()

            // Cache and emit fresh results
            cacheManager.cacheMovies(category, page, freshMovies)

            // Avoid pointless re-emits if data didn't change
            if (freshMovies != cachedMovies) {
                emit(Resource.Success(freshMovies))
            }
            Log.d("MovieRepository", "Loaded from API: $category page $page")
        } catch (e: HttpException) {
            Log.e("MovieRepository", "HTTP Error ($category page $page): ${e.message}")
            emit(
                Resource.Error(
                    message = e.localizedMessage ?: "An unexpected error occurred",
                    data = cachedMovies
                )
            )
        } catch (e: IOException) {
            Log.e("MovieRepository", "Network Error ($category page $page): ${e.message}")
            emit(
                Resource.Error(
                    message = "No internet connection. Please check your network.",
                    data = cachedMovies
                )
            )
        } catch (e: Exception) {
            Log.e("MovieRepository", "Unknown Error ($category page $page): ${e.message}")
            emit(
                Resource.Error(
                    message = e.localizedMessage ?: "An unexpected error occurred",
                    data = cachedMovies
                )
            )
        }
    }

    override fun getPopularMovies(page: Int, forceRefresh: Boolean): Flow<Resource<List<Movie>>> =
        cachedThenFetchMovies(
            category = "popular",
            page = page,
            forceRefresh = forceRefresh,
            fetchFromApi = {
                api.getPopularMovies(page = page).results.map { it.toMovie() }
            }
        )

    override fun getTopRatedMovies(page: Int, forceRefresh: Boolean): Flow<Resource<List<Movie>>> =
        cachedThenFetchMovies(
            category = "top_rated",
            page = page,
            forceRefresh = forceRefresh,
            fetchFromApi = {
                api.getTopRatedMovies(page = page).results.map { it.toMovie() }
            }
        )

    override fun getNowPlayingMovies(page: Int, forceRefresh: Boolean): Flow<Resource<List<Movie>>> =
        cachedThenFetchMovies(
            category = "now_playing",
            page = page,
            forceRefresh = forceRefresh,
            fetchFromApi = {
                api.getNowPlayingMovies(page = page).results.map { it.toMovie() }
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