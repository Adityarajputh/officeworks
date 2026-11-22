package moc.itivo.officeworks.enquiry.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import moc.itivo.officeworks.enquiry.data.DummyJsonEnquiryApi
import moc.itivo.officeworks.enquiry.data.DummyJsonEnquiryGateway
import moc.itivo.officeworks.enquiry.domain.EnquiryGateway
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Singleton

/** Swap point for enquiry delivery: bind a backend-backed gateway here in production. */
@Module
@InstallIn(SingletonComponent::class)
abstract class EnquiryModule {

    @Binds
    abstract fun bindEnquiryGateway(impl: DummyJsonEnquiryGateway): EnquiryGateway

    companion object {
        @Provides
        @Singleton
        fun provideDummyJsonEnquiryApi(retrofit: Retrofit): DummyJsonEnquiryApi = retrofit.create()
    }
}
