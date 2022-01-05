package io.flaterlab.smsbroadcast.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.flaterlab.smsbroadcast.domain.SmsBroadcastRepository
import io.flaterlab.smsbroadcast.domain.SmsBroadcastRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
interface BindingModule {

    @Binds
    fun bindSmsBroadcastRepository(impl: SmsBroadcastRepositoryImpl): SmsBroadcastRepository
}