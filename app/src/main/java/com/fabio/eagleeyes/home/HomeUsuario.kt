package com.fabio.eagleeyes.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope

import com.fabio.eagleeyes.R
import com.fabio.eagleeyes.alerta.activitiy.AlertasActivity
import com.fabio.eagleeyes.login.LoginActivity
import com.fabio.eagleeyes.global.FirebaseConfig
import com.fabio.eagleeyes.pai.ListaFilhosActivity

import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeUsuario : AppCompatActivity() {

}