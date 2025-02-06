package com.empresa.aplicacion

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import javax.inject.Inject
import javax.inject.Singleton
import androidx.hilt.navigation.compose.hiltViewModel
import com.empresa.aplicacion.ui.theme.Tarea1_MartinezPerezJavierTheme


@Module
class DatabaseModulo2 {
    @Provides
    @Singleton
    fun pruebaDatabase(app: Application?) {
        return AppDatabase.getInstance(app)
    }

    @Provides
    fun pruebaUsuarioDao(db: AppDatabase): UsuarioDao {
        return db.usuarioDao()
    }
}


// Hilt
@HiltAndroidApp
class App : Application()

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val edad: Int,
    val intereses: String
)

@Dao
interface UsuarioDao {
    @Insert
    suspend fun insertar(usuario: Usuario)

    @Query("SELECT * FROM usuarios")
    fun obtenerTodos(): StateFlow<List<Usuario>>
}

@Database(entities = [Usuario::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao

    companion object {
        fun getInstance(app: Application?) {

        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object moduloDatabase {
    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(app, AppDatabase::class.java, "app_database").build()
    }

    @Provides
    fun provideUsuarioDao(db: AppDatabase): UsuarioDao = db.usuarioDao()
}

interface ApiService {
    @GET("users")
    suspend fun getUsers(): List<Usuario>
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)
}

@HiltViewModel
class UsuarioViewModel @Inject constructor(
    private val usuarioDao: UsuarioDao,
    private val apiService: ApiService
) : ViewModel() {
    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios

    init {
        cargarUsuarios()
    }

    fun cargarUsuarios() {
        viewModelScope.launch {
            _usuarios.value = apiService.getUsers()
        }
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: UsuarioViewModel = hiltViewModel()
            Tarea1_MartinezPerezJavierTheme {
                MyScreen(viewModel)
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyScreen(viewModel: UsuarioViewModel) {
    val usuarios by viewModel.usuarios.collectAsState()
    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text("Conecta2: Jóvenes y Mayores") },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = Color(0xFF00BCD4))
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        MainContent(Modifier.padding(innerPadding), usuarios)
    }
}

@Composable
fun MainContent(modifier: Modifier = Modifier, usuarios: List<Usuario>) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Conecta2: Jóvenes y Mayores...",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Image(
            painter = painterResource(id = R.drawable.chico_ayudando_a_viejo),
            contentDescription = "Joven ayudando a viejo con el movil",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(bottom = 16.dp)
        )

        LazyColumn {
            items(usuarios.size) { index ->
                Text(
                    text = "${usuarios[index].nombre} (${usuarios[index].edad})\nIntereses: ${usuarios[index].intereses}",
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
        }
    }
}
