package com.example.instafire

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.example.instafire.databinding.ActivityCreateBinding
import com.example.instafire.models.Post
import com.example.instafire.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

private const val TAG="CreateActivity"
class CreateActivity : AppCompatActivity() {

    private var signedInUser: User? = null
    private var photoUri: Uri? = null
    private lateinit var binding: ActivityCreateBinding
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storageReference=FirebaseStorage.getInstance().reference
        firestoreDb = Firebase.firestore

        val userReference = firestoreDb.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get()
            .addOnSuccessListener { userSnapshot ->
                signedInUser = userSnapshot.toObject(User::class.java)
                Log.i(TAG, "signed in user is $signedInUser")
            }
            .addOnFailureListener { exception ->
                Log.i(TAG, "Failure fetching in signed in user", exception)
            }

        val getaction = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback { uri ->
                photoUri = uri
                binding.ivChoosedImage.setImageURI(photoUri)
            }
        )



        binding.btnPickImage.setOnClickListener {
            Log.i(TAG, "Open up image picker on the device")
            getaction.launch("image/*")
//            val imagePickerIntent= Intent(Intent.ACTION_GET_CONTENT)
//            imagePickerIntent.type="image/*"
//
//            if (imagePickerIntent.resolveActivity(packageManager)!=null){
//
//            }
        }
        binding.btnSubmit.setOnClickListener {
            handleSubmitButton()
        }


    }

    private fun handleSubmitButton() {
        if (photoUri == null) {
            Toast.makeText(this, "Please Choose an Image", Toast.LENGTH_SHORT).show()
            return
        }
        if (binding.etDescription.text.toString().isBlank()){
            Toast.makeText(this, "Description cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        if(signedInUser== null)
        {
            Toast.makeText(this, "No signed in user, please wait", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSubmit.isEnabled=false
        val photoReference=storageReference.child("image/${System.currentTimeMillis()}-photo.jpg")
        photoReference.putFile(photoUri as Uri)
            .continueWithTask{photoUploadTask->
                Log.i(TAG,"uploaded bytes: ${photoUploadTask.result?.bytesTransferred}")
                photoReference.downloadUrl
            }.continueWithTask{downloadUrlTask ->
                val post= Post(
                    binding.etDescription.text.toString(),
                    downloadUrlTask.result.toString(),
                    System.currentTimeMillis(),
                    signedInUser)
                firestoreDb.collection("posts").add(post)
            }.addOnCompleteListener { postCreationTask->
                binding.btnSubmit.isEnabled=true
                if (!postCreationTask.isSuccessful){
                    Log.e(TAG,"Exception during firebaser operations", postCreationTask.exception)
                    Toast.makeText(this, "failed to save this post", Toast.LENGTH_SHORT).show()
                }
                binding.etDescription.text.clear()
                binding.ivChoosedImage.setImageResource(0)
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                val profileIntent= Intent(this,ProfileActivity::class.java)
                profileIntent.putExtra(USER_NAME,signedInUser?.name)
                startActivity(profileIntent)
                finish()
            }
    }

}
