package uz.jbnuu.support.ui.historynotification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import uz.jbnuu.support.databinding.HistoryFragmentBinding

@AndroidEntryPoint
class HistoryNotification : Fragment() {

lateinit var binding:HistoryFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = HistoryFragmentBinding.inflate(inflater)
        return binding.root
    }

}