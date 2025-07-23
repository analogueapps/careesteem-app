/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.visits.viewmodel

import android.app.Activity
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aits.careesteem.network.ErrorHandler
import com.aits.careesteem.network.Repository
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.DateTimeUtils
import com.aits.careesteem.utils.NetworkUtils
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.visits.model.TodoListResponse
import com.google.gson.JsonElement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class ToDoViewModel @Inject constructor(
    private val repository: Repository,
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor,
    private val errorHandler: ErrorHandler,
) : ViewModel() {

    // LiveData for UI
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _toDoList = MutableLiveData<List<TodoListResponse.Data>>()
    val toDoList: LiveData<List<TodoListResponse.Data>> get() = _toDoList

    private val _completeCount = MutableLiveData<Int>().apply { value = 0 }
    val completeCount: LiveData<Int> get() = _completeCount

    private val _totalCount = MutableLiveData<Int>().apply { value = 0 }
    val totalCount: LiveData<Int> get() = _totalCount

    fun getToDoList(activity: Activity, visitDetailsId: String) {
        _toDoList.value = emptyList()
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Check if network is available before making the request
                if (!NetworkUtils.isNetworkAvailable(activity)) {
                    AlertUtils.showToast(
                        activity,
                        "No Internet Connection. Please check your network and try again.",
                        ToastyType.ERROR
                    )
                    return@launch
                }

                val response = repository.getToDoList(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .toString(),
                    visitDetailsId = visitDetailsId
                )

                if (response.isSuccessful) {
                    response.body()?.let { list ->
                        _toDoList.value = list.data.sortedByDescending { it.todoEssential }
                        //_completeCount.value = list.data.count { it.todoEssential }
                        _totalCount.value = list.data.count { it.todoEssential }
                        _completeCount.value =
                            list.data.count { it.todoEssential && it.todoOutcome.isNotEmpty() }
                    }
                } else {
                    if (response.code() == 404) {
                        return@launch
                    }
                    errorHandler.handleErrorResponse(response, activity)
                }
            } catch (e: SocketTimeoutException) {
                AlertUtils.showToast(
                    activity,
                    "Request Timeout. Please try again.",
                    ToastyType.ERROR
                )
            } catch (e: HttpException) {
                AlertUtils.showToast(activity, "Server error: ${e.message}", ToastyType.ERROR)
            } catch (e: Exception) {
                AlertUtils.showToast(activity, "An error occurred: ${e.message}", ToastyType.ERROR)
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTodo(
        activity: Activity,
        todoOutcome: Int,
        clientId: String,
        visitDetailsId: String,
        todoDetailsId: String,
        carerNotes: String,
        todoEssential: Boolean
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Check if network is available before making the request
                if (!NetworkUtils.isNetworkAvailable(activity)) {
                    AlertUtils.showToast(
                        activity,
                        "No Internet Connection. Please check your network and try again.",
                        ToastyType.ERROR
                    )
                    return@launch
                }

                val response = repository.updateTodoDetails(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .toString(),
                    todoId = todoDetailsId,
                    carerNotes = carerNotes,
                    todoOutcome = todoOutcome
                )

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val jsonElement: JsonElement? = responseBody
                    val jsonObject = JSONObject(jsonElement.toString())
                    AlertUtils.showToast(
                        activity,
                        jsonObject.optString("message"),
                        ToastyType.SUCCESS
                    )
                } else {
                    errorHandler.handleErrorResponse(response, activity)
                }
            } catch (e: SocketTimeoutException) {
                AlertUtils.showToast(
                    activity,
                    "Request Timeout. Please try again.",
                    ToastyType.ERROR
                )
            } catch (e: HttpException) {
                AlertUtils.showToast(activity, "Server error: ${e.message}", ToastyType.ERROR)
            } catch (e: Exception) {
                AlertUtils.showToast(activity, "An error occurred: ${e.message}", ToastyType.ERROR)
                e.printStackTrace()
            } finally {
                _isLoading.value = false
                getToDoList(
                    activity = activity,
                    visitDetailsId = visitDetailsId
                )
                if (todoOutcome == 0) {
                    if (todoEssential) {
                        automaticAlerts(
                            activity = activity,
                            todoDetailsId = todoDetailsId,
                            visitDetailsId = visitDetailsId,
                            clientId = clientId,
                        )
                    }
                }
            }
        }
    }

    private fun automaticAlerts(
        activity: Activity,
        todoDetailsId: String,
        visitDetailsId: String,
        clientId: String,
    ) {
        viewModelScope.launch {
            try {
                // Check if network is available before making the request
                if (!NetworkUtils.isNetworkAvailable(activity)) {
                    AlertUtils.showToast(
                        activity,
                        "No Internet Connection. Please check your network and try again.",
                        ToastyType.ERROR
                    )
                    return@launch
                }

                val response = repository.automaticTodoAlerts(
                    hashToken = sharedPreferences.getString(SharedPrefConstant.HASH_TOKEN, null)
                        .toString(),
                    todoDetailsId = todoDetailsId,
                    visitDetailsId = visitDetailsId,
                    clientId = clientId,
                    alertType = "To Do Notcompleted",
                    alertStatus = "Action Required",
                    createdAt = DateTimeUtils.getCurrentTimestampGMT()
                )

                if (response.isSuccessful) {
                    //_isCheckOutEligible.value = true
                } else {
                    //errorHandler.handleErrorResponse(response, activity)
                }
            } catch (e: SocketTimeoutException) {
                AlertUtils.showLog("activity", "Request Timeout. Please try again.")
            } catch (e: HttpException) {
                AlertUtils.showLog("activity", "Server error: ${e.message}")
            } catch (e: Exception) {
                AlertUtils.showLog("activity", "An error occurred: ${e.message}")
                e.printStackTrace()
            }
        }
    }

}