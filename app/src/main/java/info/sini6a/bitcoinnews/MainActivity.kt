package info.sini6a.bitcoinnews

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.Gravity
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.google.gson.GsonBuilder
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import okhttp3.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.io.IOException
import java.lang.Exception
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class MainActivity : AppCompatActivity() {

    var index : Int = 0
    var numberOfArticles : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        toast("Attempting to fetch JSON.")
        fetchData()

        fab.setOnClickListener { view ->
            Toast.makeText(applicationContext,"Reading next article (${index.plus(2)} of ${numberOfArticles.plus(1)})", Toast.LENGTH_SHORT).show()

            index = fetchData()

        }


    }

    fun fetchData(): Int {

        val url = "https://newsapi.org/v2/everything?language=en&q=bitcoin&sortBy=publishedAt&apiKey=704674f49da84ffa9e9d86e2eb8f9c5c"

        val request = Request.Builder().url(url).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                toast("Failed to execute request: " + e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                println(body)

                val gson = GsonBuilder().create()
                val news = gson.fromJson(body, News::class.java)
                numberOfArticles = news.articles.size

                runOnUiThread {
                    textView_title.text = news.articles[index].title

                    val cal : Calendar = Calendar.getInstance()
                    val tz : TimeZone = cal.timeZone
                    val sdf : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                    val outputSdf : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
                    outputSdf.timeZone = tz
                    sdf.calendar = cal
                    cal.time = sdf.parse(news.articles[index].publishedAt)
                    val humanReadable : String = outputSdf.format(cal.time)

                    textView_publishedAt.text = humanReadable
                    textView_source.text = news.articles[index].source.name
                    textView_content.text = news.articles[index].content

                    val image = imageView
                    Picasso.get().load(news.articles[index].urlToImage)
                        .fit().centerCrop()
                        .error(R.drawable.ic_launcher_background)
                        .placeholder(R.drawable.progress_animation)
                        .into(image)

                    textView_url.text = news.articles[index].url
                }

            }

        })

        if(index < numberOfArticles - 1) {
            index++
        }
        return index
    }

    class News(val articles: List<Article>)

    class Source(val name: String)

    class Article(val title: String, val content: String, val url: String, val urlToImage: String, val publishedAt: String, val source: Source)

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            R.id.action_about -> {
                startActivity(object: Intent(this, AboutActivity::class.java){})
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
