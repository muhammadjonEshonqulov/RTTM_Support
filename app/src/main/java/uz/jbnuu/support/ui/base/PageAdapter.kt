package uz.jbnuu.support.ui.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import uz.jbnuu.support.ui.News.NewsFragment

class PageAdapter(fm: FragmentManager, var data: ArrayList<NewsFragment>) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {

        return data[position]
    }

    override fun getCount() = data.size


}