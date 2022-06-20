package ru.pl.criminalintent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.pl.criminalintent.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



            //Вместо этого - android:name="ru.pl.criminalintent.details.CrimeDetailsFragment" в .xml
/*        val fragment = CrimeDetailsFragment()
        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_container, fragment)
            .commit()*/
    }
}