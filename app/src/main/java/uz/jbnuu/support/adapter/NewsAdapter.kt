package uz.jbnuu.support.adapter

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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
            } else if (data.status == 2){
                binding.ticketNumber.setTextColor(binding.root.context.getColor(R.color.closed_tab_color))
                binding.titleTicket.setTextColor(binding.root.context.getColor(R.color.closed_tab_color))
                binding.statusTicket.setTextColor(binding.root.context.getColor(R.color.closed_tab_color))
            }

            data.id?.let {
                binding.ticketNumber.text = "$it"
            }
            data.title?.let {
                binding.titleTicket.text = it.substring(0,1).uppercase()+it.substring(1).lowercase()
            }
            data.status?.let {
                when (it) {
                    0 -> {
                        binding.statusTicket.text = "O'qilmagan"
                    }
                    1 -> {
                        binding.statusTicket.text = "O'qilgan"
                    }
                    2 -> {
                        binding.statusTicket.text = "Yopilgan"
                    }
                }
            }
            binding.topicItem.setOnClickListener {
                listener.onItemClick(data)
            }
            data.chat_count?.let {
                if(it > 0){
                    binding.unReadTicketCount.visibility = View.VISIBLE
                    if (data.status == 0){
                        binding.unReadTicketCountLay.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.new_tab_color))
                    } else if (data.status == 1){
                        binding.unReadTicketCountLay.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.un_closed_tab_color))
                    } else if (data.status == 2){
                        binding.unReadTicketCountLay.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.closed_tab_color))
                    }
                    binding.unReadTicketCount.text = "$it"
                    var titleTicket = SpannableString(binding.titleTicket.text)
                    titleTicket.setSpan(StyleSpan(Typeface.BOLD), 0, titleTicket.length, 0)
                    binding.titleTicket.text = titleTicket
                    val statusTicket = SpannableString("O'qilmagan")
                    statusTicket.setSpan(StyleSpan(Typeface.BOLD), 0, statusTicket.length, 0)
                    binding.statusTicket.text = statusTicket
                    val ticketNumber = SpannableString(binding.ticketNumber.text)
                    ticketNumber.setSpan(StyleSpan(Typeface.BOLD), 0, ticketNumber.length, 0)
                    binding.ticketNumber.text = ticketNumber

                } else {
                    binding.unReadTicketCount.visibility = View.GONE
                }
            }
        }
    }
}
