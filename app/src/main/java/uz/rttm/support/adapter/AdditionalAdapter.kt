package uz.rttm.support.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import uz.rttm.support.databinding.ItemAdditionalBinding
import uz.rttm.support.ui.sendApplication.TechItem
import uz.rttm.support.utils.MyDiffUtil


class AdditionalAdapter(val listener: OnItemClickListener) : RecyclerView.Adapter<AdditionalAdapter.MyViewHolder>() {

    private var data = emptyList<TechItem>()

    fun setData(newData: List<TechItem>) {
        val diffUtil = MyDiffUtil(data, newData)
        val diffUtilResult = DiffUtil.calculateDiff(diffUtil)
        data = newData
        diffUtilResult.dispatchUpdatesTo(this)
    }

    interface OnItemClickListener {
        fun onItemClick(data: TechItem, type: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding: ItemAdditionalBinding = ItemAdditionalBinding.inflate(LayoutInflater.from(parent.context), parent, false)// DataBindingUtil.inflate(LayoutInflater.from(parent.context),R.layout.item_all_notification, parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    inner class MyViewHolder(private val binding: ItemAdditionalBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(data: TechItem) {
            binding.name.text = data.tip.text
            binding.status.text = if(data.status == 1)  "Bor" else "Yo'q"
            binding.eligibility.text = if(data.condition == 1)  "Yaroqsiz" else if (data.condition == 2) "Nosoz" else "Soz"
        }
    }
}
