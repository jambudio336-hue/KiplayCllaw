package com.k1play.agentpro.di

import android.content.Context
import androidx.room.Room
import com.k1play.agentpro.memory.MemoryDb
import com.k1play.agentpro.memory.MemoryFactDao
import com.k1play.agentpro.memory.MessageDao
import com.k1play.agentpro.provider.AnthropicProvider
import com.k1play.agentpro.provider.GeminiProvider
import com.k1play.agentpro.provider.OpenAIProvider
import com.k1play.agentpro.provider.OpenRouterProvider
import com.k1play.agentpro.tools.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMemoryDb(@ApplicationContext context: Context): MemoryDb {
        return Room.databaseBuilder(
            context,
            MemoryDb::class.java,
            "k1play_agent_memory"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideMessageDao(db: MemoryDb): MessageDao = db.messageDao()

    @Provides
    fun provideMemoryFactDao(db: MemoryDb): MemoryFactDao = db.memoryFactDao()

    @Provides
    fun provideScheduledTaskDao(db: MemoryDb): com.k1play.agentpro.scheduler.ScheduledTaskDao = db.scheduledTaskDao()

    @Provides
    @Singleton
    fun provideAnthropicProvider(): AnthropicProvider = AnthropicProvider()

    @Provides
    @Singleton
    fun provideOpenAIProvider(): OpenAIProvider = OpenAIProvider()

    @Provides
    @Singleton
    fun provideGeminiProvider(): GeminiProvider = GeminiProvider()

    @Provides
    @Singleton
    fun provideOpenRouterProvider(): OpenRouterProvider = OpenRouterProvider()

    @Provides
    @Singleton
    fun provideToolRegistry(
        smsRead: SmsReadTool,
        smsSend: SmsSendTool,
        phoneCall: PhoneCallTool,
        phoneLog: PhoneLogTool,
        contactsSearch: ContactsSearchTool,
        contactsAdd: ContactsAddTool,
        calendarQuery: CalendarQueryTool,
        calendarCreate: CalendarCreateTool,
        location: LocationTool,
        cameraSnap: CameraSnapTool,
        cameraRecord: CameraRecordTool,
        notification: NotificationTool,
        clipboardRead: ClipboardReadTool,
        clipboardWrite: ClipboardWriteTool,
        fileRead: FileReadTool,
        fileWrite: FileWriteTool,
        fileList: FileListTool,
        scriptExec: ScriptExecTool,
        termuxExec: TermuxExecTool,
        sensor: SensorTool,
        settings: SettingsTool,
        browserOpen: BrowserOpenTool,
        browserSearch: BrowserSearchTool,
        appLaunch: AppLaunchTool,
        appAutomate: AppAutomateTool,
        screenRead: ScreenReadTool,
        emailSend: EmailSendTool,
        screenCapture: ScreenCaptureTool,
        visionAnalyze: VisionAnalyzeTool,
        notificationListen: NotificationListenTool,
        schedulerTool: SchedulerTool,
        messagingOpen: MessagingOpenTool,
        messagingRead: MessagingReadTool,
        messagingReply: MessagingReplyTool,
        heartbeatContext: HeartbeatContextTool
    ): ToolRegistry {
        return ToolRegistry().apply {
            register(
                smsRead, smsSend,
                phoneCall, phoneLog,
                contactsSearch, contactsAdd,
                calendarQuery, calendarCreate,
                location,
                cameraSnap, cameraRecord,
                notification,
                clipboardRead, clipboardWrite,
                fileRead, fileWrite, fileList,
                scriptExec, termuxExec,
                sensor,
                settings,
                browserOpen, browserSearch,
                appLaunch, appAutomate, screenRead,
                emailSend,
                screenCapture, visionAnalyze,
                notificationListen,
                schedulerTool,
                messagingOpen, messagingRead, messagingReply,
                heartbeatContext
            )
        }
    }
}
