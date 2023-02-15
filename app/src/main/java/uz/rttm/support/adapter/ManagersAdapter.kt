package uz.rttm.support.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import uz.rttm.support.R
import uz.rttm.support.databinding.ItemManagersBinding
import uz.rttm.support.utils.MyDiffUtil


class ManagersAdapter(val listener: OnItemClickListener) : RecyclerView.Adapter<ManagersAdapter.MyViewHolder>() {

    private var data = emptyList<Map<String, String>>()

    fun setData(newData: List<Map<String, String>>) {
        val diffUtil = MyDiffUtil(data, newData)
        val diffUtilResult = DiffUtil.calculateDiff(diffUtil)
        data = newData
        diffUtilResult.dispatchUpdatesTo(this)
    }

    interface OnItemClickListener {
        fun onItemClick(data: Map<String, String>, type: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding: ItemManagersBinding = ItemManagersBinding.inflate(LayoutInflater.from(parent.context), parent, false)// DataBindingUtil.inflate(LayoutInflater.from(parent.context),R.layout.item_all_notification, parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    inner class MyViewHolder(private val binding: ItemManagersBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(data: Map<String, String>) {
            binding.firstName.text = data["first_name"]
            binding.lastName.text = data["last_name"]
            binding.middleName.text = data["middle_name"]
            binding.phone.text = data["phone"]
            binding.telegram.text = "@" + data["telegram"]?.split("t.me/")?.last()

            Glide
                .with(binding.root.context)
                .load(data["image"])
                .placeholder(R.mipmap.ic_launcher_round)
                .into(binding.imgUser)

            binding.phone.setOnClickListener {
                listener.onItemClick(data, 1)
            }
            binding.telegram.setOnClickListener {
                listener.onItemClick(data, 2)
            }
        }
    }
}
