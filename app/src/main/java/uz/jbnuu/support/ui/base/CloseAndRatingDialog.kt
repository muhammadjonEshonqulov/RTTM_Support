package uz.jbnuu.support.ui.base

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import uz.jbnuu.support.R
import uz.jbnuu.support.databinding.DialogCloseAndRatingBinding

class CloseAndRatingDialog : AlertDialog {
    private var text: TextView? = null


    var submit_listener_onclick: (() -> Unit)? = null
    var cancel_listener_onclick: (() -> Unit)? = null
    var rating = 0

    lateinit var binding: DialogCloseAndRatingBinding

    fun setOnSubmitClick(l: (() -> Unit)?) {
        submit_listener_onclick = l
    }


    fun setOnCancelClick(l: (() -> Unit)?) {
        cancel_listener_onclick = l
    }

    constructor(context: Context) : super(context) {
        this.setCancelable(true)
        val view =
            LayoutInflater.from(context).inflate(R.layout.dialog_close_and_rating, null, false)
        binding = DialogCloseAndRatingBinding.bind(view)



        view?.apply {

            binding.submit.setOnClickListener {
                submit_listener_onclick?.invoke()
            }
            binding.cancel.setOnClickListener {
                cancel_listener_onclick?.invoke()
            }
            binding.star1.setOnClickListener {
                rating = 1
                binding.star1.setImageResource(R.drawable.ic_star_rated)
                binding.star2.setImageResource(R.drawable.ic_star)
                binding.star3.setImageResource(R.drawable.ic_star)
                binding.star4.setImageResource(R.drawable.ic_star)
                binding.star5.setImageResource(R.drawable.ic_star)
            }
            binding.star2.setOnClickListener {

                rating = 2
                binding.star1.setImageResource(R.drawable.ic_star_rated)
                binding.star2.setImageResource(R.drawable.ic_star_rated)
                binding.star3.setImageResource(R.drawable.ic_star)
                binding.star4.setImageResource(R.drawable.ic_star)
                binding.star5.setImageResource(R.drawable.ic_star)
            }
            binding.star3.setOnClickListener {
                rating = 3
                binding.star1.setImageResource(R.drawable.ic_star_rated)
                binding.star2.setImageResource(R.drawable.ic_star_rated)
                binding.star3.setImageResource(R.drawable.ic_star_rated)
                binding.star4.setImageResource(R.drawable.ic_star)
                binding.star5.setImageResource(R.drawable.ic_star)
            }
            binding.star4.setOnClickListener {

                rating = 4
                binding.star1.setImageResource(R.drawable.ic_star_rated)
                binding.star2.setImageResource(R.drawable.ic_star_rated)
                binding.star3.setImageResource(R.drawable.ic_star_rated)
                binding.star4.setImageResource(R.drawable.ic_star_rated)
                binding.star5.setImageResource(R.drawable.ic_star)
            }
            binding.star5.setOnClickListener {
                rating = 5
                binding.star1.setImageResource(R.drawable.ic_star_rated)
                binding.star2.setImageResource(R.drawable.ic_star_rated)
                binding.star3.setImageResource(R.drawable.ic_star_rated)
                binding.star4.setImageResource(R.drawable.ic_star_rated)
                binding.star5.setImageResource(R.drawable.ic_star_rated)
            }
        }
        setView(view)
    }


}