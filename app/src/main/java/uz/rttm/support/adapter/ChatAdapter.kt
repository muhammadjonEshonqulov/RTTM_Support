package uz.rttm.support.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.json.JSONArray
import uz.rttm.support.databinding.ItemChatBinding
import uz.rttm.support.models.chat.ChatData
import uz.rttm.support.ui.sendApplication.TechItem
import uz.rttm.support.utils.Constants.Companion.BASE_URL_IMG
import uz.rttm.support.utils.MyDiffUtil
import uz.rttm.support.utils.formatDateStr
import uz.rttm.support.utils.lg

class ChatAdapter(val listener: OnItemClickListener) : RecyclerView.Adapter<ChatAdapter.MyViewHolder>() {

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
        val binding: ItemChatBinding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)// zBindingUtil.inflate(LayoutInflater.from(parent.context),R.layout.item_all_notification, parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    inner class MyViewHolder(private val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(data: ChatData) {
            binding.additional.visibility = View.GONE
            binding.titleChatManager.visibility = View.GONE



            if (data.user?.role == "1") {   //  role = 1   User
                binding.userHasChat.visibility = View.VISIBLE
                binding.managerHasChat.visibility = View.GONE
                data.updated_at?.let {
                    lg("updated_at in adapter -> " + it)
                    binding.dateChatUser.text = formatDateStr(it)
                }
                if (data.file?.isNotEmpty() == true) {
                    binding.userImg.visibility = View.VISIBLE
                    data.file.let {
                        Glide
                            .with(binding.root.context)
                            .load(BASE_URL_IMG + it)
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
                if (data.user.name?.isNotEmpty() == true && data.user.fam?.isNotEmpty() == true) {
                    binding.userName.text = "" + data.user.name.first().uppercase() + "." + data.user.fam.uppercase()
                }
            } else if (data.user?.role == "2" || data.user?.role == "3") {

                binding.userHasChat.visibility = View.GONE
                binding.managerHasChat.visibility = View.VISIBLE

                data.updated_at?.let {
                    binding.dateChatManager.text = formatDateStr(it)
                }
                data.text?.let {
                    binding.messageChatManager.text = it
                }
                if (data.file?.isNotEmpty() == true) {
                    binding.managerImg.visibility = View.VISIBLE
                    data.file.let {
                        Glide
                            .with(binding.root.context)
                            .load(BASE_URL_IMG + it)
                            .into(binding.managerImg)
                    }
                } else {
                    binding.managerImg.visibility = View.GONE
                }
                data.user.lavozim?.let {
                    binding.managerLavozimName.text = it
                }
                data.user.bolim?.name?.let {
                    binding.managerBolimName.text = it
                }
                if (data.user.name?.isNotEmpty() == true && data.user.fam?.isNotEmpty() == true) {
                    binding.managerName.text = "" + data.user.name.first().uppercase() + "." + data.user.fam.uppercase()
                }

            }


            if (data.text?.startsWith("additional#") == true) {
                try {
                    binding.additional.visibility = View.VISIBLE
                    binding.titleChatManager.visibility = View.VISIBLE

                    val myKeyTech = data.text.split("#")
                    binding.textPaste.text = myKeyTech[1]
                    binding.stateOfTech.text = myKeyTech[2]
                    binding.building.text = myKeyTech[3]
                    binding.room.text = myKeyTech[4]

                    val data1 = parseJsonToList(myKeyTech[5])

                    val adapter = AdditionalAdapter(object : AdditionalAdapter.OnItemClickListener {
                        override fun onItemClick(data: TechItem, type: Int) {

                        }

                    })
                    binding.listAdditional.adapter = adapter
                    binding.listAdditional.layoutManager = LinearLayoutManager(binding.room.context)
                    adapter.setData(data1)
                    binding.messageChatManager.text = myKeyTech[6]
                } catch (e: Exception) {
                    lg("additional error -> ${e.message}")
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

    private fun parseJsonToList(jsonString: String): List<TechItem> {
        val jsonArray = JSONArray(jsonString)
        val techItems = mutableListOf<TechItem>()

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val techItem = TechItem(
                _id = jsonObject.getString("_id"),
                tip = TechItem.Tip(
                    _id = jsonObject.getJSONObject("tip").getString("_id"),
                    text = jsonObject.getJSONObject("tip").getString("text"),
                    __v = jsonObject.getJSONObject("tip").getInt("__v")
                ),
                condition = jsonObject.getInt("condition"),
                status = jsonObject.getInt("status")
            )
            techItems.add(techItem)
        }

        return techItems
    }

}
