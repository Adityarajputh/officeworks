package moc.itivo.officeworks.catalogue.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import moc.itivo.officeworks.catalogue.data.DummyJsonProductRepository
import moc.itivo.officeworks.catalogue.data.remote.DummyJsonProductApi
import moc.itivo.officeworks.catalogue.domain.FeaturedProductsProvider
import moc.itivo.officeworks.catalogue.domain.ProductRepository
import moc.itivo.officeworks.catalogue.domain.TopRatedFeaturedProductsProvider
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Singleton

/** The swap points for product data. Changing the data source means changing these bindings. */
@Module
@InstallIn(SingletonComponent::class)
abstract class CatalogueModule {

    @Binds
    abstract fun bindProductRepository(impl: DummyJsonProductRepository): ProductRepository

    @Binds
    abstract fun bindFeaturedProductsProvider(impl: TopRatedFeaturedProductsProvider): FeaturedProductsProvider

    companion object {
        @Provides
        @Singleton
        fun provideDummyJsonProductApi(retrofit: Retrofit): DummyJsonProductApi = retrofit.create()
    }
}
