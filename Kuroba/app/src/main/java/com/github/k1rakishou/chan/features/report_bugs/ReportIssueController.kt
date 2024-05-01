package com.github.k1rakishou.chan.features.report_bugs

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.github.k1rakishou.chan.R
import com.github.k1rakishou.chan.core.di.component.controller.ControllerComponent
import com.github.k1rakishou.chan.core.manager.GlobalWindowInsetsManager
import com.github.k1rakishou.chan.core.manager.ReportManager
import com.github.k1rakishou.chan.core.manager.WindowInsetsListener
import com.github.k1rakishou.chan.features.toolbar.BackArrowMenuItem
import com.github.k1rakishou.chan.features.toolbar.ToolbarMiddleContent
import com.github.k1rakishou.chan.features.toolbar.ToolbarText
import com.github.k1rakishou.chan.ui.compose.components.KurobaComposeCheckbox
import com.github.k1rakishou.chan.ui.compose.components.KurobaComposeCustomTextField
import com.github.k1rakishou.chan.ui.compose.components.KurobaComposeText
import com.github.k1rakishou.chan.ui.compose.ktu
import com.github.k1rakishou.chan.ui.compose.providers.ComposeEntrypoint
import com.github.k1rakishou.chan.ui.compose.providers.LocalChanTheme
import com.github.k1rakishou.chan.ui.controller.LoadingViewController
import com.github.k1rakishou.chan.ui.controller.base.Controller
import com.github.k1rakishou.chan.ui.controller.base.DeprecatedNavigationFlags
import com.github.k1rakishou.chan.utils.AppModuleAndroidUtils
import com.github.k1rakishou.chan.utils.BackgroundUtils
import com.github.k1rakishou.common.ModularResult
import com.github.k1rakishou.core_logger.LogStorage
import com.github.k1rakishou.core_logger.Logger
import com.github.k1rakishou.core_themes.ThemeEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.Duration
import javax.inject.Inject

// TODO: Fix insets if this controller is ever enabled again.
class ReportIssueController(
  context: Context
) : Controller(context), WindowInsetsListener {

  @Inject
  lateinit var globalWindowInsetsManager: GlobalWindowInsetsManager
  @Inject
  lateinit var reportManager: ReportManager
  @Inject
  lateinit var themeEngine: ThemeEngine

  private val issueNumberState = mutableStateOf("")
  private val reportTitleState = mutableStateOf("")
  private val reportDescriptionState = mutableStateOf("")
  private val reportLogsState = mutableStateOf<TextFieldValue?>(null)
  private val attachLogsState = mutableStateOf(true)

  override fun injectControllerDependencies(component: ControllerComponent) {
    component.inject(this)
  }

  override fun onCreate() {
    super.onCreate()

    updateNavigationFlags(
      newNavigationFlags = DeprecatedNavigationFlags()
    )

    toolbarState.enterDefaultMode(
      leftItem = BackArrowMenuItem(
        onClick = { requireNavController().popController() }
      ),
      middleContent = ToolbarMiddleContent.Title(
        title = ToolbarText.Id(R.string.report_controller_report_an_error_problem)
      ),
      menuBuilder = {
        withMenuItem(
          id = ACTION_SEND_REPORT,
          drawableId = R.drawable.ic_send_white_24dp,
          onClick = { onSendReportClick() }
        )
      }
    )

    view = ComposeView(context).apply {
      setContent {
        ComposeEntrypoint {
          BuildContent()
        }
      }
    }

    onInsetsChanged()
    globalWindowInsetsManager.addInsetsUpdatesListener(this)
  }

  @Composable
  private fun BuildContent() {
    val chanTheme = LocalChanTheme.current

    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(chanTheme.backColorCompose)
        .padding(all = 8.dp)
    ) {
      var issueNumber by issueNumberState
      var reportTitle by reportTitleState
      var reportDescription by reportDescriptionState
      var attachLogs by attachLogsState
      var attachLogsWasReset by remember { mutableStateOf(false) }
      val issueNumberKbOptions = remember { KeyboardOptions(keyboardType = KeyboardType.Number) }

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight()
          .background(chanTheme.accentColorCompose.copy(alpha = 0.5f))
      ) {
        KurobaComposeText(
          modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(4.dp),
          text = stringResource(id = R.string.report_controller_note),
          fontSize = 12.ktu
        )
      }

      KurobaComposeCustomTextField(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight(),
        value = issueNumber,
        textColor = chanTheme.textColorPrimaryCompose,
        parentBackgroundColor = chanTheme.backColorCompose,
        maxLines = 1,
        singleLine = true,
        maxTextLength = 10,
        labelText = stringResource(id = R.string.report_controller_issue_number),
        keyboardOptions = issueNumberKbOptions,
        onValueChange = { number -> issueNumber = number }
      )

      Spacer(modifier = Modifier.height(8.dp))

      KurobaComposeCustomTextField(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight(),
        value = reportTitle,
        enabled = issueNumber.isEmpty(),
        textColor = chanTheme.textColorPrimaryCompose,
        parentBackgroundColor = chanTheme.backColorCompose,
        maxLines = 1,
        singleLine = true,
        maxTextLength = ReportManager.MAX_TITLE_LENGTH,
        labelText = stringResource(id = R.string.report_controller_i_have_a_problem_with),
        onValueChange = { title -> reportTitle = title }
      )

      Spacer(modifier = Modifier.height(8.dp))

      KurobaComposeCustomTextField(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight(),
        value = reportDescription,
        textColor = chanTheme.textColorPrimaryCompose,
        parentBackgroundColor = chanTheme.backColorCompose,
        maxLines = 4,
        maxTextLength = ReportManager.MAX_DESCRIPTION_LENGTH,
        labelText = stringResource(id = R.string.report_controller_problem_description),
        onValueChange = { description -> reportDescription = description }
      )

      Spacer(modifier = Modifier.height(8.dp))

      KurobaComposeCheckbox(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight(),
        text = stringResource(id = R.string.report_controller_attach_logs),
        currentlyChecked = attachLogs,
        onCheckChanged = { checked -> attachLogs = checked }
      )

      Spacer(modifier = Modifier.height(8.dp))

      LaunchedEffect(key1 = issueNumber, block = {
        if (issueNumber.isNotEmpty() && !attachLogsWasReset) {
          attachLogs = false
          attachLogsWasReset = true
        }
      })

      if (attachLogs) {
        var reportLogsMut by reportLogsState
        val reportLogs = reportLogsMut

        LaunchedEffect(
          key1 = Unit,
          block = {
            val logsAsAnnotatedString = withContext(Dispatchers.IO) {
              val logs = Logger.selectLogs<AnnotatedString>(
                duration = Duration.standardMinutes(2),
                logLevels = arrayOf(
                  LogStorage.LogLevel.Warning,
                  LogStorage.LogLevel.Debug,
                  LogStorage.LogLevel.Error,
                ),
                logSortOrder = LogStorage.LogSortOrder.Ascending,
                logFormatter = LogStorage.composeFormatter()
              )

              if (logs.isNullOrEmpty()) {
                return@withContext AnnotatedString("")
              }

              return@withContext buildAnnotatedString {
                append(logs)
                append(reportManager.getReportFooter(context))
              }
            }

            reportLogsMut = TextFieldValue(logsAsAnnotatedString)
          }
        )

        if (reportLogs != null) {
          KurobaComposeCustomTextField(
            modifier = Modifier
              .fillMaxWidth()
              .wrapContentHeight(),
            textColor = chanTheme.textColorPrimaryCompose,
            parentBackgroundColor = chanTheme.backColorCompose,
            value = reportLogs,
            maxTextLength = ReportManager.MAX_LOGS_LENGTH,
            fontSize = 12.ktu,
            onValueChange = { logs -> reportLogsMut = logs }
          )
        }
      }
    }
  }

  override fun onInsetsChanged() {
    // TODO: if this screen is ever used again
//    val bottomPaddingDp = calculateBottomPaddingForRecyclerInDp(
//      globalWindowInsetsManager = globalWindowInsetsManager
//    )
//
//    view.updatePaddings(bottom = dp(bottomPaddingDp.toFloat()))
  }

  override fun onDestroy() {
    super.onDestroy()

    globalWindowInsetsManager.removeInsetsUpdatesListener(this)
  }

  private fun onSendReportClick() {
    val issueNumberString = issueNumberState.value
    if (issueNumberString.isNotEmpty()) {
      sendComment(issueNumberString)
    } else {
      sendActualReport()
    }
  }

  private fun sendComment(issueNumberString: String) {
    val issueNumber = issueNumberString.toIntOrNull() ?: -1
    if (issueNumber < 0) {
      showToast("Bad issue number: \'$issueNumberString\'")
      return
    }

    val description = reportDescriptionState.value.take(ReportManager.MAX_DESCRIPTION_LENGTH)
    if (description.isEmpty()) {
      showToast(R.string.report_controller_description_cannot_be_empty_error_comment_mode)
      return
    }

    val logs = if (attachLogsState.value) {
      reportLogsState.value?.text?.takeLast(ReportManager.MAX_LOGS_LENGTH)
    } else {
      null
    }

    val loadingController = LoadingViewController(context, true)
    presentController(loadingController)

    reportManager.sendComment(
      issueNumber = issueNumber,
      description = description,
      logs = logs,
      onReportSendResult = { result ->
        BackgroundUtils.ensureMainThread()
        loadingController.stopPresenting()

        when (result) {
          is ModularResult.Value -> {
            handleResult(result)
          }
          is ModularResult.Error -> {
            Logger.e(TAG, "Send report error", result.error)

            val errorMessage = result.error.message ?: "No error message"
            val formattedMessage = AppModuleAndroidUtils.getString(
              R.string.report_controller_error_while_trying_to_send_report,
              errorMessage
            )

            AppModuleAndroidUtils.showToast(context, formattedMessage)
          }
        }
      }
    )
  }

  private fun sendActualReport() {
    val title = reportTitleState.value.take(ReportManager.MAX_TITLE_LENGTH)
    if (title.isEmpty()) {
      showToast(R.string.report_controller_title_cannot_be_empty_error)
      return
    }

    val logs = if (attachLogsState.value) {
      reportLogsState.value?.text?.takeLast(ReportManager.MAX_LOGS_LENGTH)
    } else {
      null
    }

    if (attachLogsState.value && logs.isNullOrEmpty()) {
      showToast(R.string.report_controller_logs_are_empty_error)
      return
    }

    val description = reportDescriptionState.value.take(ReportManager.MAX_DESCRIPTION_LENGTH)
    if (description.isEmpty() && logs.isNullOrEmpty()) {
      showToast(R.string.report_controller_description_cannot_be_empty_error)
      return
    }

    val loadingController = LoadingViewController(context, true)
    presentController(loadingController)

    reportManager.sendReport(
      title = title,
      description = description,
      logs = logs,
      onReportSendResult = { result ->
        BackgroundUtils.ensureMainThread()
        loadingController.stopPresenting()

        when (result) {
          is ModularResult.Value -> {
            handleResult(result)
          }
          is ModularResult.Error -> {
            Logger.e(TAG, "Send report error", result.error)

            val errorMessage = result.error.message ?: "No error message"
            val formattedMessage = AppModuleAndroidUtils.getString(
              R.string.report_controller_error_while_trying_to_send_report,
              errorMessage
            )

            AppModuleAndroidUtils.showToast(context, formattedMessage)
          }
        }
      }
    )
  }

  private fun handleResult(result: ModularResult<Unit>) {
    when (result) {
      is ModularResult.Value -> {
        AppModuleAndroidUtils.showToast(context, R.string.report_controller_report_sent_message)
        onFinished()
      }
      is ModularResult.Error -> {
        val errorMessage = result.error.message ?: "No error message"
        val formattedMessage = AppModuleAndroidUtils.getString(
          R.string.report_controller_error_while_trying_to_send_report,
          errorMessage
        )

        AppModuleAndroidUtils.showToast(context, formattedMessage)
      }
    }
  }

  private fun onFinished() {
    this.navigationController!!.popController()
  }

  companion object {
    private const val TAG = "ReportProblemController"

    private const val ACTION_SEND_REPORT = 1
  }
}