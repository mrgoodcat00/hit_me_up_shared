package com.mrgoodcat.hitmeup.domain.usecase.login

import com.mrgoodcat.hitmeup.di.IoDispatcher
import com.mrgoodcat.hitmeup.domain.model.AppSettingsModel
import com.mrgoodcat.hitmeup.domain.repository.DbRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class IsUserVerifiedUseCase @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val dbRepository: DbRepository
) {
    suspend fun execute(updateVerified: Boolean? = null): Boolean {
        return withContext(ioDispatcher) {
            var settings = dbRepository.getAppSettings() ?: AppSettingsModel()

            if (updateVerified != null) {
                settings = settings.copy(isUserVerified = updateVerified)

                if (dbRepository.getAppSettings() == null) {
                    dbRepository.insertAppSettings(settings)
                } else {
                    dbRepository.updateAppSettings(settings)
                }

                updateVerified
            } else {
                settings.isUserVerified
            }
        }
    }
}