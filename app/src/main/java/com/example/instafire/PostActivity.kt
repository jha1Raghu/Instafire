package com.example.instafire

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instafire.databinding.ActivityPostBinding
import com.example.instafire.models.Post
import com.example.instafire.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private const val TAG="PostActivity"
const val USER_NAME="USER_NAME"
open class PostActivity : AppCompatActivity() {

    private var signedInUser: User?=null
    lateinit var binding: ActivityPostBinding
    lateinit var firestoreDb:FirebaseFirestore
    lateinit var posts: MutableList<Post>
    lateinit var postAdapter:PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Create a single post layout file
        // Create data source
        posts= mutableListOf()
        // Create adapter
        postAdapter= PostAdapter(this,posts)
        // Bind adapter and the layout manager to the rv
        binding.rvPosts.adapter=postAdapter
        binding.rvPosts.layoutManager=LinearLayoutManager(this)

        firestoreDb= Firebase.firestore

        val userReference=firestoreDb.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get()
            .addOnSuccessListener { userSnapshot->
                signedInUser=userSnapshot.toObject(User::class.java)
                Log.i(TAG,"signed in user is $signedInUser")
            }
            .addOnFailureListener{exception->
                Log.i(TAG,"Failure fetching in signed in user", exception)
            }

        var postsReference=firestoreDb
            .collection("posts")
            .limit(20)
            .orderBy("creation_time_ms",Query.Direction.DESCENDING)

        val username= intent.getStringExtra(USER_NAME)
        if(username!=null){
            supportActionBar?.title= username
          postsReference=  postsReference.whereEqualTo("user.name",username)
        }

        postsReference.addSnapshotListener{ snapshot, exception ->
            if (snapshot==null || exception!=null){
                Log.e(TAG,"Exception when querying posts", exception)
                return@addSnapshotListener
            }
            val postsList=snapshot.toObjects(Post::class.java)
            posts.clear()
            posts.addAll(postsList)
            postAdapter.notifyDataSetChanged()
            for (post in postsList){
                Log.i(TAG,"Post $post")
            }
        }
        binding.fabCreate.setOnClickListener {
            val intent=Intent(this,CreateActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_posts,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.profile){
            val intent= Intent(this,ProfileActivity::class.java)
            intent.putExtra(USER_NAME,"${signedInUser?.name}")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}