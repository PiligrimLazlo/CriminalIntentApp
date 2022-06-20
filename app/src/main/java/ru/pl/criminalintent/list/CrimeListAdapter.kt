package ru.pl.criminalintent.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import ru.pl.criminalintent.data.Crime
import ru.pl.criminalintent.databinding.ListItemCrimeBinding
import ru.pl.criminalintent.databinding.ListItemCrimeRequirePoliceBinding
import java.lang.IllegalArgumentException
import java.util.*

class CrimeListAdapter(
    private val crimes: List<Crime>,
    private val onCrimeClicked: (crimeId: UUID) -> Unit
) : RecyclerView.Adapter<BaseHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            Crime.NOT_REQUIRES_POLICE -> {
                val binding = ListItemCrimeBinding.inflate(inflater, parent, false)
                CrimeHolder(binding)
            }
            Crime.REQUIRES_POLICE -> {
                val binding = ListItemCrimeRequirePoliceBinding.inflate(inflater, parent, false)
                CrimeHolderRequiresPolice(binding)
            }
            else -> throw IllegalArgumentException("Police constant must be 0 or 1")
        }
    }

    override fun onBindViewHolder(holder: BaseHolder, position: Int) {
        val crime = crimes[position]
        holder.bind(crime, onCrimeClicked)
    }

    override fun getItemCount(): Int = crimes.size

    override fun getItemViewType(position: Int): Int {
        return crimes[position].isRequiresPolice()
    }

}


sealed class BaseHolder(private val binding: ViewBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private lateinit var crime: Crime

    open fun bind(crime: Crime, onCrimeClicked: (crimeId: UUID) -> Unit) {
        this.crime = crime

        setListener(onCrimeClicked)

    }

    private fun setListener(onCrimeClicked: (crimeId: UUID) -> Unit) {
        binding.root.setOnClickListener {
            onCrimeClicked(crime.id)
        }
    }
}

class CrimeHolder(private val binding: ListItemCrimeBinding) : BaseHolder(binding) {

    override fun bind(crime: Crime, onCrimeClicked: (crimeId: UUID) -> Unit) {
        super.bind(crime, onCrimeClicked)

        binding.crimeTitle.text = crime.title
        binding.crimeDate.text = crime.getFormattedStringCrimeDate()

        binding.crimeSolved.visibility =
            if (crime.isSolved) View.VISIBLE else View.GONE
    }
}

class CrimeHolderRequiresPolice(private val binding: ListItemCrimeRequirePoliceBinding) :
    BaseHolder(binding) {

    override fun bind(crime: Crime, onCrimeClicked: (crimeId: UUID) -> Unit) {
        super.bind(crime, onCrimeClicked)

        binding.listItemCrimeInclude.crimeTitle.text = crime.title
        binding.listItemCrimeInclude.crimeDate.text = crime.getFormattedStringCrimeDate()

        binding.listItemCrimeInclude.crimeSolved.visibility =
            if (crime.isSolved) View.VISIBLE else View.GONE
    }
}

