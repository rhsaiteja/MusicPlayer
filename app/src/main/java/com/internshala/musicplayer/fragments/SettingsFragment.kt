package com.internshala.musicplayer.fragments


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.Switch
import com.internshala.musicplayer.R


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class SettingsFragment : Fragment() {

    var myActivity:Activity? = null
    var switchShake:Switch? = null
    object Statified{
        var MY_PREFS_NAME = "ShakeFeature"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_settings, container, false)
        switchShake = view?.findViewById(R.id.switchShake)
        activity?.title = "Settings"
        setHasOptionsMenu(true)
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        var prefs = myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME,Context.MODE_PRIVATE)
        var isAllowed = prefs?.getBoolean("feature",false)
        if(isAllowed as Boolean){
            switchShake?.isChecked = true
        }else{
            switchShake?.isChecked = false
        }
        switchShake?.setOnCheckedChangeListener({compoundButton, b ->
            if(b){
                val editor = myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME,Context.MODE_PRIVATE)?.edit()
                editor?.putBoolean("feature",true)
                editor?.apply()
            }else{
                val editor = myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME,Context.MODE_PRIVATE)?.edit()
                editor?.putBoolean("feature",false)
                editor?.apply()
            }
        })
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item2 : MenuItem? = menu?.findItem(R.id.action_sort)
        item2?.isVisible = false
    }
}
