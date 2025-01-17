package test.taylor.com.taylorcode.ui.performance.recyclerview_item_anim

import android.content.Context
import android.graphics.Canvas
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import test.taylor.com.taylorcode.kotlin.ConstraintLayout
import test.taylor.com.taylorcode.kotlin.*
import test.taylor.com.taylorcode.kotlin.extension.onItemVisibilityChange
import test.taylor.com.taylorcode.kotlin.extension.isInScreen
import test.taylor.com.taylorcode.ui.recyclerview.variety.VarietyAdapter2

class RecyclerViewItemAnimActivity : AppCompatActivity() {

    private val myAdapter by lazy {
        VarietyAdapter2().apply {
            addItemBuilder(TextProxy2())
        }
    }

    private val id by Extras("id", "0")

    private lateinit var rv: RecyclerView

    private val contentView by lazy {
        ConstraintLayout {
            layout_width = match_parent
            layout_height = match_parent

            rv = RecyclerView {
                layout_width = match_parent
                layout_height = match_parent
                layoutManager = LinearLayoutManager(this@RecyclerViewItemAnimActivity).apply { orientation = LinearLayoutManager.HORIZONTAL }
                adapter = myAdapter
            }

            TextView {
                layout_id = "tvScroll"
                layout_width = 100
                layout_height = 50
                textSize = 12f
                textColor = "#ffffff"
                text = "scrollTo"
                gravity = gravity_center
                bottom_toBottomOf = parent_id
                shape = shape {
                    corner_radius = 20
                    solid_color = "#080808"
                }
                onClick = {
                    myAdapter.notifyDataSetChanged()
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(contentView)
        val texts = mutableListOf<String>()
        Log.v("ttaylor", "id from intent=$id")
        (0 .. 200).forEach {
            texts.add(it.toString())
        }
        myAdapter.dataList = texts

        myAdapter.onViewRecycled = { holder ->
            val count = rv.recycledViewPool.getRecycledViewCount(holder.itemViewType)
            Log.v("ttaylor", "onViewRecycled() type=${holder.itemViewType} , count=${count}")
        }
        myAdapter.onFailedToRecycleView = { holder ->
            Log.v("ttaylor", "onFailedToRecycleView() type=${holder.itemViewType}")
            (holder as? TextViewHolder2)?.tv?.animate()?.cancel() // cancel animation before force recycling
            true //return true force recycle
        }

        myAdapter.onViewDetachedFromWindow = {
            val tv = (it as? TextViewHolder2)?.tv?.text
            Log.v("ttaylor33","onViewDetachedFromWindow $tv is detach")
        }

        myAdapter.onViewRecycled = {
            val tv = (it as? TextViewHolder2)?.tv?.text
            Log.v("ttaylor33","onViewRecycled $tv is recycled")
        }

        rv.onItemVisibilityChange(percent = 0.5f) { itemView, index, isVisible ->
            Log.w("ttaylor", "[dfdfsfd]RecyclerViewItemAnimActivity.onCreate[itemView, index($index), visible($isVisible)]: ")
        }
    }
}


class TextProxy2 : VarietyAdapter2.ItemBuilder<String, TextViewHolder2>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = parent.context.run {
            MyConstraintLayout(this).apply {
                layout_id = "container"
                layout_width = 100
                layout_height = 70
                background_color = "#ff00ff"

                MyTextView(context).apply {
                    layout_id = "tvChange"
                    layout_width = wrap_content
                    layout_height = wrap_content
                    textSize = 30f
                    textColor = "#000000"
                    gravity = gravity_center

                }.also { addView(it) }

                TextView {
                    layout_id = "tvChange2"
                    layout_width = wrap_content
                    layout_height = wrap_content
                    textSize = 12f
                    textColor = "#000000"
                    text = "save"
                    gravity = gravity_center
                    top_toBottomOf = "tvChange"
                    start_toStartOf = "tvChange"
                }

                View {
                    layout_id = "vBottom"
                    layout_width = 2
                    layout_height = match_parent
                    background_color = "#8e8e8e"
                    bottom_toBottomOf = parent_id
                }
            }
        }
        return TextViewHolder2(itemView)
    }

    override fun onBindViewHolder(holder: TextViewHolder2, data: String, index: Int, action: ((Any?) -> Unit)?) {
        holder.tv?.text = data
        holder.container?.tag = data
        holder.tv?.let { holder.container?.addScrollListener(it) }
        holder.tv?.let { tv ->
            holder.itemView.tag = data
//            ViewCompat.animate(tv).translationX(900f).setDuration(10000).start()// this will set transient state to true
            // ObjectAnimator wont set transient state
//            animSet {
//                animObject {
//                    target = tv
//                    translationX = floatArrayOf(0f, 900f)
//                }
//                duration = 30000L
//                interpolator = AccelerateDecelerateInterpolator()
//            }.start()
        }

    }
}

class TextViewHolder2(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val tv = itemView.find<MyTextView>("tvChange")
    val container = itemView.find<MyConstraintLayout>("container")
}

/**
 * case: a new way to listen child scroll event of ItemView
 */
class MyTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : androidx.appcompat.widget.AppCompatTextView(
    context,
    attrs,
    defStyleAttr
), RecyclerViewScrollListener {
    private var hasShown = false
    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        Log.v("ttaylor22", "draw()")
    }

    override fun onParentScroll() {
        if (this.isInScreen) {
            if (! hasShown) {
                Log.w("ttaylor", "onScroll() 111111111 text(${text}) is in screen")
                hasShown = true
            }
        } else {
            if (hasShown) {
                Log.e("ttaylor", "onScroll() 1111111111111 text(${text}) is off screen")
                hasShown = false
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.d("ttaylor","onDetachedFromWindow() ")
    }
}

class MyConstraintLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val scrollListeners = mutableListOf<RecyclerViewScrollListener>()

    fun addScrollListener(listener: RecyclerViewScrollListener) {
        scrollListeners.add(listener)
    }

    override fun offsetTopAndBottom(offset: Int) {
        super.offsetTopAndBottom(offset)
        scrollListeners.forEach { it.onParentScroll() }

    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        Log.v("ttaylor22", "onLayout() top=$top")
    }
}

interface RecyclerViewScrollListener {
    fun onParentScroll()
}