package uz.rttm.support.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import uz.rttm.support.databinding.ItemChatBinding
import uz.rttm.support.models.chat.ChatData
import uz.rttm.support.utils.Constants.Companion.BASE_URL_IMG
import uz.rttm.support.utils.MyDiffUtil
import uz.rttm.support.utils.formatDateStr

class ChatAdapter(val listener : OnItemClickListener) : RecyclerView.Adapter<ChatAdapter.MyViewHolder>() {
    
    private var data = emptyList<ChatData>()
    
    fun setData(newData: List<ChatData>) {
        val diffUtil = MyDiffUtil(data, newData)
        val diffUtilResult = DiffUtil.calculateDiff(diffUtil)
        data = newData
        diffUtilResult.dispatchUpdatesTo(this)
    }
    
    interface OnItemClickListener {
        fun onItemClick(data: ChatData)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding: ItemChatBinding = ItemChatBinding.inflate(LayoutInflater.from(parent.context),parent, false)// DataBindingUtil.inflate(LayoutInflater.from(parent.context),R.layout.item_all_notification, parent, false)
        return MyViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(data[position])
    }
    
    override fun getItemCount() :Int = data.size
    
    inner class MyViewHolder(private val binding: ItemChatBinding): RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SimpleDateFormat", "SetTextI18n")
        fun bind(data: ChatData) {
            if(data.user?.role == "1"){   //  role = 1   User
                binding.userHasChat.visibility = View.VISIBLE
                binding.managerHasChat.visibility = View.GONE
                data.updated_at?.let {
                    binding.dateChatUser.text = formatDateStr(it)
                }
                if (data.file?.isNotEmpty() == true){
                    binding.userImg.visibility = View.VISIBLE
                    data.file.let {
                        Glide
                            .with(binding.root.context)
                            .load(BASE_URL_IMG+it)
                            .into(binding.userImg)
                    }
                } else {
                    binding.userImg.visibility = View.GONE
                }

                data.text?.let {
                    binding.messageChatUser.text = it
                }
                data.user.lavozim?.let {
                    binding.userLavozimName.text = it
                }
                data.user.bolim?.name?.let {
                    binding.userBolimName.text = it
                }
                if (data.user.name?.isNotEmpty() == true && data.user.fam?.isNotEmpty() == true){
                    binding.userName.text = ""+data.user.name.first().uppercase()+"."+data.user.fam.uppercase()
                }
            } else if (data.user?.role == "2" || data.user?.role == "3"){
                binding.userHasChat.visibility = View.GONE
                binding.managerHasChat.visibility = View.VISIBLE

                data.updated_at?.let {
                    binding.dateChatManager.text = formatDateStr(it)
                }
                data.text?.let {
                    binding.messageChatManager.text = it
                }
                if (data.file?.isNotEmpty() == true){
                    binding.managerImg.visibility = View.VISIBLE
                    data.file.let {
                        Glide
                            .with(binding.root.context)
                            .load(BASE_URL_IMG+it)
                            .into(binding.managerImg)
                    }
                } else {
                    binding.userImg.visibility = View.GONE
                }
                data.user.lavozim?.let {
                    binding.managerLavozimName.text = it
                }
                data.user.bolim?.name?.let {
                    binding.managerBolimName.text = it
                }
                if (data.user.name?.isNotEmpty() == true && data.user.fam?.isNotEmpty() == true){
                    binding.managerName.text = ""+data.user.name.first().uppercase()+"."+data.user.fam.uppercase()
                }
            }
            binding.userImg.setOnClickListener {
                listener.onItemClick(data)
            }
            binding.managerImg.setOnClickListener {
                listener.onItemClick(data)
            }
        }
    }
}
