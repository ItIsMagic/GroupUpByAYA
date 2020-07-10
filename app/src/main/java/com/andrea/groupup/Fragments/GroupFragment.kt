package com.andrea.groupup.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.andrea.groupup.Adapters.AddParticipantAdapter
import com.andrea.groupup.Adapters.ParticipantAdapter
import com.andrea.groupup.Http.*
import com.andrea.groupup.Http.Mapper.Mapper
import com.andrea.groupup.Models.Group
import com.andrea.groupup.Models.User

import com.andrea.groupup.R
import com.android.volley.VolleyError
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import kotlin.properties.Delegates

/**
 * A simple [Fragment] subclass.
 */
class GroupFragment : BaseFragment() {

    private lateinit var http: Http
    lateinit var group: Group
    lateinit var user: User
    lateinit var token: String
    var isAdmin: Boolean = false

    lateinit var addAdapter: AddParticipantAdapter
    lateinit var userRes: List<User>
    var addListItems = arrayListOf<User>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_group, container, false)

        group = ACTIVITY.group
        user = ACTIVITY.user
        token = ACTIVITY.token
        http = Http(ACTIVITY)
        val listItems = arrayListOf<User>()

        for (member in group.members) {
            if(user.id == member.id){
                isAdmin = member.UserGroup.is_admin
            }
            listItems.add(member)
        }

        val adapter = ParticipantAdapter(listItems, user, isAdmin, group.id, token, requireContext())
        val listView: ListView = view.findViewById(R.id.listOfParticipants)
        listView.adapter = adapter

        val addUser: ImageView = view.findViewById(R.id.addUser)

        addUser.setOnClickListener{
            UserHttp(ACTIVITY).getAll(object: VolleyCallbackArray {
                override fun onResponse(array: JSONArray) {
                    val dialog = BottomSheetDialog(ACTIVITY, R.style.DialogStyle)

                    val view = layoutInflater.inflate(R.layout.dialog_add_user, null)
                    val textView: TextView = view.findViewById(R.id.userNone)
                    textView.visibility = View.GONE
                    addListItems.clear()

                    userRes = Mapper().mapper(array)
                    for (user: User in userRes){
                        addListItems.add(user)
                    }
                    addListItems.removeAll(listItems)

                    if(addListItems.isEmpty()){
                        textView.visibility = View.VISIBLE
                    }
                    addAdapter = AddParticipantAdapter(addListItems, requireContext())
                    val addListView: ListView = view.findViewById(R.id.addListOfParticipants)
                    addListView.adapter = addAdapter

                    addListView.onItemClickListener = object : AdapterView.OnItemClickListener {
                        override fun onItemClick(parent: AdapterView<*>, view: View,
                                                 position: Int, id: Long) {
                            addListItems[position]
                            GroupHttp(http).addToGroup(group.id.toString(), addListItems[position].id.toString(), token, object:VolleyCallback {
                                override fun onResponse(jsonObject: JSONObject) {
                                    val gson: Gson = Gson()
                                    val listGroup = arrayListOf<Group>()
                                    listGroup.add(gson.fromJson(jsonObject.toString(), Group::class.java))

                                    listItems.clear()
                                    for (member in listGroup[0].members) {
                                        listItems.add(member)
                                    }

                                    listView.invalidateViews();
                                    dialog.dismiss()
                                }

                                override fun onError(error: VolleyError) {
                                    Log.e("ADD TO GROUP", "add to group - onError")
                                    Log.e("ADD TO GROUP", error.toString())
                                }
                            })
                        }
                    }

                    // SEARCH INIT
                    val searchView: SearchView = view.findViewById(R.id.searchBar)
                    searchView.queryHint = getString(R.string.searchHint)
                    searchView.imeOptions = EditorInfo.IME_ACTION_DONE

                    searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String): Boolean {
                            return false
                        }

                        override fun onQueryTextChange(newText: String): Boolean {
                            addAdapter.filter(newText)
                            return false
                        }
                    })
                    dialog.setContentView(view)
                    dialog.show()
                }


                override fun onError(error: VolleyError) {
                    Log.e("USER", "login - onError")
                    Log.e("USER", error.toString())
                }
            })
        }

        /*listView.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(parent: AdapterView<*>, view: View,
                                     position: Int, id: Long) {
                val intent = Intent(requireContext(), EventActivity::class.java)
                startActivity(intent)
            }
        }*/
        return view
    }

    fun loadListItem(){

    }

}
