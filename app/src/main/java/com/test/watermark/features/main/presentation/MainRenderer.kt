package com.test.watermark.features.main.presentation

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.test.watermark.*
import com.test.watermark.encoder.TextureMovieEncoder
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainRenderer(
    private val context: Context,
    private val camera: MainCamera,
    private val glSurfaceView: GLSurfaceView
) : GLSurfaceView.Renderer {

    private var screenState: MainScreenState = MainScreenState.WaitRecording
    private var width: Int = DEFAULT_WIDTH
    private var height: Int = DEFAULT_HEIGHT

    private var textureMovieEncoder: TextureMovieEncoder? = null

    private val vertexCords = floatArrayOf(
        1f, 1f,
        -1f, 1f,
        -1f, -1f,
        1f, 1f,
        -1f, -1f,
        1f, -1f
    )

    private val vertexCordsOrder = floatArrayOf(
        1f, 1f, 0f, 1f, 0f, 0f,
        1f, 1f, 0f, 0f,
        1f, 0f
    )

    private val transformMatrix = FloatArray(16)

    private var programHandle = 0
    private var vertexHandle = 0
    private var fragmentHandle = 0
    private var vertexPositionHandle = 0
    private var vertexMatrixHandle = 0
    private var textureOESHandle = 0
    private var vertexCoordinateHandle = 0

    private var vertexBuffer: FloatBuffer? = null
    private var vertexOrderBuffer: FloatBuffer? = null

    private var surfaceTextureId = -1
    private var surfaceTexture: SurfaceTexture? = null

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        textureMovieEncoder = TextureMovieEncoder()
        createSurfaceTexture()

        vertexBuffer = ByteBuffer.allocateDirect(vertexCords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexBuffer?.put(vertexCords)?.position(0)

        vertexOrderBuffer = ByteBuffer.allocateDirect(vertexCordsOrder.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexCordsOrder)
        vertexOrderBuffer?.position(0)

        programHandle = GLES20.glCreateProgram()
        vertexHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)

        val vertexShader = readShaderFromResource(context, R.raw.vertex_shader)
        GLES20.glShaderSource(vertexHandle, vertexShader)
        GLES20.glCompileShader(vertexHandle)
        GLES20.glAttachShader(programHandle, vertexHandle)

        fragmentHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)

        val fragmentShader = readShaderFromResource(context, R.raw.fragment_shader)
        GLES20.glShaderSource(fragmentHandle, fragmentShader)
        GLES20.glCompileShader(fragmentHandle)
        GLES20.glAttachShader(programHandle, fragmentHandle)
        GLES20.glLinkProgram(programHandle)
    }

    override fun onSurfaceChanged(p0: GL10?, p1: Int, p2: Int) {
        GLES20.glViewport(0, 0, p1, p2)
    }

    override fun onDrawFrame(p0: GL10?) {
        when (screenState) {
            is MainScreenState.StartRecording -> {
                textureMovieEncoder?.startRecording(
                    TextureMovieEncoder.EncoderConfig(
                        File(context.filesDir, FILE_NAME),
                        width,
                        height,
                        DEFAULT_BIT_RATE,
                        EGL14.eglGetCurrentContext()
                    )
                )
                screenState = MainScreenState.Recording
            }
            is MainScreenState.StopRecording -> {
                textureMovieEncoder?.stopRecording()
                screenState = MainScreenState.WaitRecording
            }
            else -> Unit
        }

        surfaceTexture?.updateTexImage()
        textureMovieEncoder?.setTextureId(surfaceTextureId)
        textureMovieEncoder?.frameAvailable(surfaceTexture)
        surfaceTexture?.getTransformMatrix(transformMatrix)

        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 0.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glUseProgram(programHandle)

        vertexPositionHandle = GLES20.glGetAttribLocation(programHandle, "avVertex")
        vertexCoordinateHandle = GLES20.glGetAttribLocation(programHandle, "avVertexCoordinate")
        vertexMatrixHandle = GLES20.glGetUniformLocation(programHandle, "umTransformMatrix")
        textureOESHandle = GLES20.glGetUniformLocation(programHandle, "usTextureOes")

        GLES20.glVertexAttribPointer(
            vertexPositionHandle,
            2,
            GLES20.GL_FLOAT,
            false,
            8,
            vertexBuffer
        )
        GLES20.glVertexAttribPointer(
            vertexCoordinateHandle,
            2,
            GLES20.GL_FLOAT,
            false,
            8,
            vertexOrderBuffer
        )

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, surfaceTextureId)
        GLES20.glUniform1i(textureOESHandle, 0)
        GLES20.glUniformMatrix4fv(vertexMatrixHandle, 1, false, transformMatrix, 0)
        GLES20.glEnableVertexAttribArray(vertexPositionHandle)
        GLES20.glEnableVertexAttribArray(vertexCoordinateHandle)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6)
        GLES20.glDisableVertexAttribArray(vertexPositionHandle)
        GLES20.glDisableVertexAttribArray(vertexCoordinateHandle)
    }

    fun setScreenState(state: MainScreenState) {
        screenState = state
    }

    fun stop() {
        surfaceTexture?.release()
        surfaceTexture = null
        textureMovieEncoder = null
        GLES20.glDeleteProgram(programHandle)
    }

    private fun createSurfaceTexture() {
        surfaceTextureId = createOESTextureObject()
        surfaceTexture = SurfaceTexture(surfaceTextureId)
        camera.startPreview(surfaceTexture, width, height)
        surfaceTexture?.setOnFrameAvailableListener { glSurfaceView.requestRender() }
    }

    private fun createOESTextureObject(): Int {
        val tex = IntArray(1)
        GLES20.glGenTextures(1, tex, 0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0])
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST.toFloat()
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE.toFloat()
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE.toFloat()
        )
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
        return tex[0]
    }

    private fun readShaderFromResource(context: Context, resourceId: Int): String {
        val builder = StringBuilder()
        var `is`: InputStream? = null
        var isr: InputStreamReader? = null
        var br: BufferedReader? = null
        try {
            `is` = context.resources.openRawResource(resourceId)
            isr = InputStreamReader(`is`)
            br = BufferedReader(isr)
            var line: String?
            while (br.readLine().also { line = it } != null) {
                builder.append(line).append("\n")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                `is`?.close()
                isr?.close()
                br?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return builder.toString()
    }
}
