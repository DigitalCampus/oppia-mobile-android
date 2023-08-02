package org.digitalcampus.oppia.api

object Paths {
    // server path vars - new version
    const val OPPIAMOBILE_API = "api/v2/"
    const val LEADERBOARD_PATH = OPPIAMOBILE_API + "leaderboard/"
    const val SERVER_AWARDS_PATH = OPPIAMOBILE_API + "awards/"
    const val TRACKER_PATH = OPPIAMOBILE_API + "tracker/"
    const val SERVER_TAG_PATH = OPPIAMOBILE_API + "tag/"
    const val SERVER_COURSES_PATH = OPPIAMOBILE_API + "course/"
    const val COURSE_ACTIVITY_PATH = SERVER_COURSES_PATH + "%s/activity/"
    const val COURSE_INFO_PATH = SERVER_COURSES_PATH + "%s"
    const val QUIZ_SUBMIT_PATH = OPPIAMOBILE_API + "quizattempt/"
    const val RESET_PATH = OPPIAMOBILE_API + "reset/"
    const val REMEMBER_USERNAME_PATH = OPPIAMOBILE_API + "username/"
    const val REGISTER_PATH = OPPIAMOBILE_API + "register/"
    const val LOGIN_PATH = OPPIAMOBILE_API + "user/"
    const val ACTIVITYLOG_PATH = "api/activitylog/"
    const val SERVER_INFO_PATH = "server/"
    const val UPDATE_PROFILE_PATH = OPPIAMOBILE_API + "profileupdate/"
    const val DELETE_ACCOUNT_PATH = OPPIAMOBILE_API + "deleteaccount/"
    const val DOWNLOAD_ACCOUNT_DATA_PATH = OPPIAMOBILE_API + "downloaddata/"
    const val CHANGE_PASSWORD_PATH = OPPIAMOBILE_API + "password/"
    const val USER_COHORTS_PATH = OPPIAMOBILE_API + "cohorts/"
    const val USER_PROFILE_PATH = OPPIAMOBILE_API + "profile/"
}