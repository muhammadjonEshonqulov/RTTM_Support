package uz.rttm.support.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import uz.rttm.support.R
import uz.rttm.support.databinding.CustomSpinnerItemBinding
import uz.rttm.support.models.login.Bolim

class DepartmentAdapter(val context: Context, var dataSource: List<Bolim>) : BaseAdapter() {

    lateinit var binding: CustomSpinnerItemBinding

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View
        val vh: ItemHolder
        if (convertView == null) {
            view = inflater.inflate(
                R.layout.custom_spinner_item, parent, false
            )
            binding = CustomSpinnerItemBinding.bind(view)


            vh = ItemHolder(view)
            context.let {

                vh.label.text = "${dataSource.get(position).name}"
            }
            view?.tag = vh
        } else {
            view = convertView
            vh = view.tag as ItemHolder
        }
        return view
    }


}

private class ItemHolder(row: View?) {
    val label: TextView = row?.findViewById(R.id.name_department) as TextView
}
