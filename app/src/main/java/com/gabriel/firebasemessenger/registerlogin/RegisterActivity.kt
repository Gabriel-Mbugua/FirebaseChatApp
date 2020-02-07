package com.gabriel.firebasemessenger.registerlogin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gabriel.firebasemessenger.R
import com.gabriel.firebasemessenger.messages.LatestMessagesActivity
import com.gabriel.firebasemessenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private val TAG = "RegisterActivity"
    private val REQUEST_CODE = 0
    private var loading = false

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var database: FirebaseDatabase

    var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        database = FirebaseDatabase.getInstance()

        select_photo_button_register.setOnClickListener {
            Log.d(TAG, "Try to show photo selector!")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE)
        }

        register_button_register.setOnClickListener {
            createNewUser()
        }

        already_have_account_text_view.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        //Check if user is signed in
        val currentUser = auth.currentUser
        if(currentUser != null){
            val intent = Intent(this, LatestMessagesActivity::class.java)
            startActivity(intent)
        }
        return
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            //proceed and check what the selected image was...
            Log.d(TAG, "Photo was selected!")
            selectedPhotoUri = data.data // represents the location of the image

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            select_photo_imageview_register.setImageBitmap(bitmap)
            select_photo_button_register.alpha = 0f
        }
    }

    private fun createNewUser() {
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()

        progress_bar_register.isIndeterminate

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter your email and password", Toast.LENGTH_LONG).show()
            return
        }

        Log.d(TAG, "\nUsername: $email Passowrd: $password")

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                Log.d(TAG, "createUserWithEmail:success")
                Log.d(TAG, user.toString())

                uploadImageToFirebaseStorage()
            } else {
                Log.d(TAG, "createUserWithEmail:failure", task.exception)
                Toast.makeText(this, "Authentication failed: ${task.exception}", Toast.LENGTH_LONG).show()
            }
        }
            .addOnFailureListener(this) {
                Log.d(TAG, "Failed to create user: ${it.message}")
            }
    }

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null)
            return

        val filename = UUID.randomUUID().toString()//random string
        val ref = storage.getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
            Log.d(TAG, "Successfully uploaded image: ${it.metadata?.path}")
            ref.downloadUrl.addOnSuccessListener {
                Log.d(TAG, "File Location: $it")

                saveUserToFirebaseDatabase(it.toString())
            }
        }
            .addOnFailureListener(this) {
                Toast.makeText(this, "Image upload failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = auth.uid ?: ""
        val ref = database.getReference("/users/$uid")

        val user = User(uid, username_edittext_register.text.toString(), profileImageUrl)

        ref.setValue(user).addOnSuccessListener {
            Log.d(TAG, "User saved to database  ")

            val intent = Intent(this, LatestMessagesActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
            .addOnFailureListener{
                Toast.makeText(this, "User details mot saved: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }


}


