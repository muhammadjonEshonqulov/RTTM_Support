package uz.jbnuu.support.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import uz.jbnuu.support.R
import uz.jbnuu.support.databinding.MainFragmentBinding
import uz.jbnuu.support.utils.Prefs
import uz.jbnuu.support.utils.snack
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment(), View.OnClickListener {

    lateinit var binding: MainFragmentBinding
    @Inject
    lateinit var prefs: Prefs
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = MainFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.sendNotification.setOnClickListener(this)
        binding.settingsNotification.setOnClickListener(this)
        binding.logout.setOnClickListener(this)
        binding.historyNotification.setOnClickListener(this)

    }

    override fun onClick(p0: View?) {
        when (p0) {
            binding.sendNotification -> {
                if (findNavController()?.currentDestination?.id == R.id.mainFragment) {
                    findNavController().navigate(R.id.action_mainFragment_to_notificationsFragment)
                }
            }
            binding.logout -> {
                prefs.clear()
                requireActivity().finish()
            }
            binding.settingsNotification -> {
                snack(binding.root, "Settings")
            }
            binding.historyNotification -> {
                snack(binding.root, "History Notifications")
            }
        }
    }
}