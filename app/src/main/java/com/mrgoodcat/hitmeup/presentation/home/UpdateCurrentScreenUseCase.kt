package com.mrgoodcat.hitmeup.presentation.home

import com.mrgoodcat.hitmeup.domain.model.AppSettingsModel
import com.mrgoodcat.hitmeup.domain.repository.DbRepository
import javax.inject.Inject

class UpdateCurrentScreenUseCase @Inject constructor(
    private val dbRepository: DbRepository
) {
    suspend fun execute(screenRoute: String, chatId: String = "") {
        val settings = dbRepository.getAppSettings() ?: AppSettingsModel()

        var settingsToUpdate = settings.copy(currentScreen = screenRoute)
        settingsToUpdate = settingsToUpdate.copy(currentOpenedChatId = chatId)

        if (dbRepository.getAppSettings() == null) {
            dbRepository.insertAppSettings(settingsToUpdate)
        } else {
            dbRepository.updateAppSettings(settingsToUpdate)
        }
    }
}