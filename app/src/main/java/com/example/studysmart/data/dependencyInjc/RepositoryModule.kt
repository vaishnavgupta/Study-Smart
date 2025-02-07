package com.example.studysmart.data.dependencyInjc

import com.example.studysmart.data.repository.SessionRepositoryImpl
import com.example.studysmart.data.repository.SubjectRepositoryImpl
import com.example.studysmart.data.repository.TaskRepositoryImpl
import com.example.studysmart.domain.repository.SessionRepository
import com.example.studysmart.domain.repository.SubjectRepository
import com.example.studysmart.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindSubjectRepoWithImpl(
        impl:SubjectRepositoryImpl
    ):SubjectRepository

    @Singleton
    @Binds
    abstract fun bindTaskRepoWithImpl(
        impl:TaskRepositoryImpl
    ):TaskRepository

    @Singleton
    @Binds
    abstract fun bindSessionRepoWithImpl(
        impl:SessionRepositoryImpl
    ):SessionRepository
}