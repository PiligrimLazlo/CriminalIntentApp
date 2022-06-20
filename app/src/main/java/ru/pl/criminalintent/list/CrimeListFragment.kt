package ru.pl.criminalintent.list

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import ru.pl.criminalintent.R
import ru.pl.criminalintent.data.Crime
import ru.pl.criminalintent.databinding.FragmentCrimeListBinding
import java.util.*


class CrimeListFragment : Fragment() {

    private var _binding: FragmentCrimeListBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val crimeListViewModel: CrimeListViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCrimeListBinding.inflate(inflater, container, false)

        binding.crimeRecyclerView.layoutManager = LinearLayoutManager(context)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //launch возвращает объект job: Job, можно делать job.cancel() в onDestroyView(),
        //но можно запускать suspend repeatOnLifecycle() - корутина будет работать только
        // в определенном состоянии (STARTED) фрагмента
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                crimeListViewModel.crimes.collect { crimes ->
                    binding.crimeRecyclerView.adapter = CrimeListAdapter(crimes) { crimeId ->
                        //findNavController().navigate(R.id.show_crime_details)

                        //создали тэг <action.../> в nav_graph.xml =>
                        // сгенерировался класс CrimeListFragmentDirections
                        findNavController()
                            .navigate(CrimeListFragmentDirections.showCrimeDetails(crimeId))
                    }
                    updateUi(crimes.isEmpty())
                }
            }
        }

        setUpMenu()

        binding.addFirstCrimeButton.setOnClickListener {
            showNewCrime()
        }

    }

    private fun setUpMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_crime_list, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.new_crime -> {
                        showNewCrime()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    //вызываем этот ↑ метод т.к. эти два ↓↓ метода deprecated

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }


    fun showNewCrime() {
        viewLifecycleOwner.lifecycleScope.launch {
            val newCrime = Crime(
                id = UUID.randomUUID(),
                title = "",
                date = Date(),
                isSolved = false
            )
            crimeListViewModel.addCrime(newCrime)
            findNavController().navigate(CrimeListFragmentDirections.showCrimeDetails(newCrime.id))
        }
    }

    private fun updateUi(areThereNoCrimes: Boolean) {
        if (areThereNoCrimes) {
            binding.noCrimesTextView.visibility = View.VISIBLE
            binding.addFirstCrimeButton.visibility = View.VISIBLE
        } else {
            binding.noCrimesTextView.visibility = View.GONE
            binding.addFirstCrimeButton.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

