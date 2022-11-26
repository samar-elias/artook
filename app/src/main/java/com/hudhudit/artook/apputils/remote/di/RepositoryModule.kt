package com.hudhudit.artook.apputils.remote.di


import com.google.firebase.database.FirebaseDatabase
import com.hudhudit.artook.apputils.remote.repostore.ChatRepositoryImp
import com.hudhudit.artook.apputils.remote.repostore.ChateRepository

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {

    @Provides
    @Singleton
    fun provideChatRepository(
        database: FirebaseDatabase,


    ): ChateRepository {
        return ChatRepositoryImp(database)
    }



}