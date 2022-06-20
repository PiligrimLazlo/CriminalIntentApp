package ru.pl.criminalintent.details

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.doOnLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.launch
import ru.pl.criminalintent.R
import ru.pl.criminalintent.data.Crime
import ru.pl.criminalintent.databinding.FragmentCrimeDetailsBinding
import ru.pl.criminalintent.utils.getScaledBitmap
import java.io.File
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList


class CrimeDetailsFragment : Fragment() {

    private var _binding: FragmentCrimeDetailsBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    //создали тэг <argument.../> в nav_graph.xml => сгенерировался класс CrimeDetailsFragmentArgs
    private val args: CrimeDetailsFragmentArgs by navArgs()

    private val crimeDetailViewModel: CrimeDetailViewModel by viewModels {
        CrimeDetailsViewModelFactory(args.crimeId)
    }

    private val selectSuspect = registerForActivityResult(
        ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        uri?.let { parseContactSelection(it) }
    }


    private val requestReadContactsPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { permissionGranted ->
        if (permissionGranted) {
            selectSuspect.launch(null)
        } else {
            showToast(getString(R.string.permission_did_not_granted))
        }
    }

    //сохраняет фото с камеры в файл по uri, предоставленного контент провайдером
    //(его создаем сами отдельно). Возвращает булеан: сохранена картинка или нет
    private val takePhoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { didTakePhoto ->
        //todo handle the result
        if (didTakePhoto && photoName != null) {
            crimeDetailViewModel.updateCrime { oldCrime ->
                oldCrime.copy(photoFileName = photoName)
            }
        }
    }

    private var photoName: String? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCrimeDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            crimeTitle.doOnTextChanged { text, _, _, _ ->
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(title = text.toString())
                }
            }

            crimeSolved.setOnCheckedChangeListener { _, isChecked ->
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(isSolved = isChecked)
                }
            }

            crimeSuspect.setOnClickListener {
                requestContactsReadPermission()
            }
            val selectSuspectIntent = selectSuspect.contract.createIntent(requireContext(), null)
            crimeSuspect.isEnabled = canResolveIntent(selectSuspectIntent)


            //создаем файл, преобразуем в photoUri => в него камера сохранит фото
            crimeCamera.setOnClickListener {
                photoName = "IMG_${Date()}.JPG"
                val photoFile = File(requireContext().applicationContext.filesDir, photoName)

                val photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "ru.pl.criminalintent.fileprovider",
                    photoFile
                )
                takePhoto.launch(photoUri)
            }
            val captureImageIntent = takePhoto.contract.createIntent(requireContext(), Uri.EMPTY)
            crimeCamera.isEnabled = canResolveIntent(captureImageIntent)

        }


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                crimeDetailViewModel.crime.collect { crime ->
                    crime?.let { updateUi(it) }
                }
            }
        }

        setFragmentResultListener(DatePickerFragment.REQUEST_KEY_DATE) { requestKey, bundle ->
            val newDate = bundle.getSerializable(DatePickerFragment.BUNDLE_KEY_DATE) as Date
            crimeDetailViewModel.updateCrime { it.copy(date = newDate) }
        }

        setFragmentResultListener(TimePickerFragment.REQUEST_KEY_DATE_TIME) { requestKey, bundle ->
            val newDateTime =
                bundle.getSerializable(TimePickerFragment.BUNDLE_KEY_DATE_TIME) as Date
            crimeDetailViewModel.updateCrime { it.copy(date = newDateTime) }
        }

        //custom back action logic and menu
        setUpMenu()
    }

    private fun updateUi(crime: Crime) {
        binding.apply {
            if (crimeTitle.text.toString() != crime.title) {
                crimeTitle.setText(crime.title)
            }
            crimeDate.text = crime.getFormattedStringCrimeDate()
            crimeDate.setOnClickListener {
                findNavController().navigate(CrimeDetailsFragmentDirections.selectDate(crime.date))
            }
            crimeSolved.isChecked = crime.isSolved

            crimeReport.setOnClickListener {
                val reportIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, getCrimeReport(crime))
                    putExtra(
                        Intent.EXTRA_SUBJECT,
                        getString(R.string.crime_report_subject)
                    )
                }
                val chooserIntent = Intent.createChooser(
                    reportIntent,
                    getString(R.string.send_report)
                )
                startActivity(chooserIntent)
            }


            crimeCall.isEnabled = crime.suspect.isNotBlank()
            crimeCall.setOnClickListener {
                val callIntent =
                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:${crime.suspectPhoneNumber}"))
                startActivity(callIntent)
            }

            crimeSuspect.text = crime.suspect.ifEmpty {
                getString(R.string.crime_suspect_text)
            }

            updatePhoto(crime.photoFileName)



            crimeTime.text = crime.getFormattedStringCrimeTime()
            crimeTime.setOnClickListener {
                findNavController().navigate(CrimeDetailsFragmentDirections.selectTime(crime.date))
            }
            //dynamic title in appbar
            setAppBarTitle(crime.title)
        }
    }

    private fun setAppBarTitle(title: String) {
        val appCompatActivity = activity as AppCompatActivity
        val appBar = appCompatActivity.supportActionBar
        appBar?.title = title
    }

    private fun setUpMenu() {
        showAppBarBackButton(true)
        setUpBackLogic()

        val menuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_details, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    android.R.id.home -> {
                        menuHost.onBackPressed()
                        true
                    }
                    R.id.delete_crime -> {
                        viewLifecycleOwner.lifecycleScope.launch {
                            crimeDetailViewModel.deleteCrime()
                        }
                        findNavController().navigateUp()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }


    private fun setUpBackLogic() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.crimeTitle.text.isNotBlank()) {
                    findNavController().navigateUp()
                } else {
                    binding.crimeTitle.error = getString(R.string.crime_title_blank_error)
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun showAppBarBackButton(isShow: Boolean) {
        //add back button in appbar
        val currentActivity = activity as AppCompatActivity
        val actionBar = currentActivity.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(isShow)
    }

    private fun getCrimeReport(crime: Crime): String {
        val solvedString = if (crime.isSolved)
            getString(R.string.crime_report_solved)
        else
            getString(R.string.crime_report_unsolved)

        val dateString = crime.getFormattedStringCrimeDate()
        val suspectText = if (crime.suspect.isBlank())
            getString(R.string.crime_report_no_suspect)
        else
            getString(R.string.crime_report_suspect, crime.suspect)

        return getString(
            R.string.crime_report,
            crime.title, dateString, solvedString, suspectText
        )
    }

    //получаем доступ к контакту по uri через контент провайдер (возвращает номер телефона)
    //не видит номера в "неправильном" формате (fixed)
    private fun parseContactSelection(contactUri: Uri) {
        //id and contact name
        var id = ""
        val queryFields =
            arrayOf(ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID)

        val queryCursor = requireActivity().contentResolver
            .query(contactUri, queryFields, null, null, null)

        queryCursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                id = cursor.getString(1)
                val suspect = cursor.getString(0)
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(suspect = suspect)
                }
            }
        }

        //phone number
        val queryFields2 =
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)

        val queryCursor2 = requireActivity().contentResolver
            .query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                queryFields2,
                "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                arrayOf(id),
                null
            )

        queryCursor2?.use { cursor ->
            if (cursor.moveToFirst()) {
                val number = cursor.getString(0)
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(suspectPhoneNumber = number)
                }
            }
        }
    }


    //проверяем есть ли активити других приложений для запуска интента.
    //Так же требует добавления queries в manifest
    private fun canResolveIntent(intent: Intent): Boolean {
        //intent.addCategory(Intent.CATEGORY_HOME) // - разкоментировать для проверки метода
        val packageManager: PackageManager = requireActivity().packageManager
        val resolvedActivity: ResolveInfo? =
            packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolvedActivity != null
    }

    private fun requestContactsReadPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
            showRationaleDialog("Error", getString(R.string.permission_did_not_granted))
        } else {
            requestReadContactsPermission.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    //обновляем фото каждый раз, когда получаем новую порцию данных от StateFlow вьюмодели
    //чтобы не грузить картинку каждый раз, используем tag
    private fun updatePhoto(photoFileName: String?) {
        if (binding.crimePhoto.tag != photoFileName) {
            val photoFile =
                photoFileName?.let { File(requireContext().applicationContext.filesDir, it) }
            if (photoFile?.exists() == true) {
                binding.crimePhoto.doOnLayout { measuredView ->
                    val scaleBitmap = getScaledBitmap(
                        photoFile.path,
                        measuredView.width,
                        measuredView.height
                    )
                    binding.crimePhoto.setImageBitmap(scaleBitmap)
                    binding.crimePhoto.tag = photoFileName
                }
            } else {
                binding.crimePhoto.setImageBitmap(null)
                binding.crimePhoto.tag = null
            }
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

    private fun showRationaleDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(message)
            .setTitle(title)
            .setCancelable(false)
            .setPositiveButton("ОК") { dialog, _ ->
                dialog.dismiss()
            }
            .create().show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        setAppBarTitle(getString(R.string.app_name))
        showAppBarBackButton(false)
    }
}