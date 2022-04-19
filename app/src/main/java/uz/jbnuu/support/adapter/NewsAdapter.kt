package uz.jbnuu.support.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import uz.jbnuu.support.R
import uz.jbnuu.support.databinding.ItemAllNotificationBinding
import uz.jbnuu.support.models.message.MessageResponse
import uz.jbnuu.support.utils.MyDiffUtil

class NewsAdapter(val listener : OnItemClickListener) : RecyclerView.Adapter<NewsAdapter.MyViewHolder>() {
    
    private var data = emptyList<MessageResponse>()
    
    fun setData(newData: List<MessageResponse>) {
        val diffUtil = MyDiffUtil(data, newData)
        val diffUtilResult = DiffUtil.calculateDiff(diffUtil)
        data = newData
        diffUtilResult.dispatchUpdatesTo(this)
    }
    
    interface OnItemClickListener {
        fun onItemClick(data: MessageResponse)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding: ItemAllNotificationBinding = ItemAllNotificationBinding.inflate(LayoutInflater.from(parent.context),parent, false)// DataBindingUtil.inflate(LayoutInflater.from(parent.context),R.layout.item_all_notification, parent, false)
        return MyViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(data[position])
    }
    
    override fun getItemCount() :Int = data.size
    
    inner class MyViewHolder(private val binding: ItemAllNotificationBinding): RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SimpleDateFormat", "SetTextI18n")
        fun bind(data: MessageResponse) {
            if (data.status == 0){
                binding.ticketNumber.setTextColor(binding.root.context.getColor(R.color.new_tab_color))
                binding.titleTicket.setTextColor(binding.root.context.getColor(R.color.new_tab_color))
                binding.statusTicket.setTextColor(binding.root.context.getColor(R.color.new_tab_color))
            } else if (data.status == 1){
                binding.ticketNumber.setTextColor(binding.root.context.getColor(R.color.un_closed_tab_color))
                binding.titleTicket.setTextColor(binding.root.context.getColor(R.color.un_closed_tab_color))
                binding.statusTicket.setTextColor(binding.root.context.getColor(R.color.un_closed_tab_color))
            }
            data.id?.let {
                binding.ticketNumber.text = "$it"
            }
            data.title?.let {
                binding.titleTicket.text = it.substring(0,1).uppercase()+it.substring(1).lowercase()
            }
            data.status?.let {
                if (it == 0){
                    binding.statusTicket.text = "O'qilmagan"
                } else if (it == 1){
                    binding.statusTicket.text = "O'qilgan"
                }
            }
            binding.topicItem.setOnClickListener {
                listener.onItemClick(data)
            }
        }
    }
}
