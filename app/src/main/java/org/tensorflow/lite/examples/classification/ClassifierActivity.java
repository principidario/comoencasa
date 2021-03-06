/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.lite.examples.classification;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.widget.Button;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.tensorflow.lite.examples.classification.env.BorderedText;
import org.tensorflow.lite.examples.classification.env.ImageUtils;
import org.tensorflow.lite.examples.classification.env.Logger;
import org.tensorflow.lite.examples.classification.tflite.Classifier;
import org.tensorflow.lite.examples.classification.tflite.ClassifierFloatMobileNet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClassifierActivity extends CameraActivity implements OnImageAvailableListener {
  private static final Logger LOGGER = new Logger();
  private static final boolean MAINTAIN_ASPECT = true;
  private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
  private static final float TEXT_SIZE_DIP = 10;
  private Bitmap rgbFrameBitmap = null;
  private Bitmap croppedBitmap = null;
  private Bitmap cropCopyBitmap = null;
  private long lastProcessingTimeMs;
  private Integer sensorOrientation;
  private Classifier classifier;
  private Matrix frameToCropTransform;
  private Matrix cropToFrameTransform;
  private BorderedText borderedText;
  private ArrayList<String> ingredients = new ArrayList<>();

  public List<Classifier.Recognition> results = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);



  }

  @Override
  protected int getLayoutId() {
    return R.layout.camera_connection_fragment;
  }

  @Override
  protected Size getDesiredPreviewFrameSize() {
    return DESIRED_PREVIEW_SIZE;
  }

  @Override
  public void onPreviewSizeChosen(final Size size, final int rotation) {
    final float textSizePx =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
    borderedText.setTypeface(Typeface.MONOSPACE);

    try {
        //classifier = new ClassifierQuantizedMobileNet(this);
        classifier = new ClassifierFloatMobileNet(this);

    } catch (IOException e) {
      LOGGER.e(e, "Failed to load classifier.");
      return;
    }

    previewWidth = size.getWidth();
    previewHeight = size.getHeight();

    sensorOrientation = rotation - getScreenOrientation();
    LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

    LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
    rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
    croppedBitmap =
        Bitmap.createBitmap(
            classifier.getImageSizeX(), classifier.getImageSizeY(), Config.ARGB_8888);

    frameToCropTransform =
        ImageUtils.getTransformationMatrix(
            previewWidth,
            previewHeight,
            classifier.getImageSizeX(),
            classifier.getImageSizeY(),
            sensorOrientation,
            MAINTAIN_ASPECT);

    cropToFrameTransform = new Matrix();
    frameToCropTransform.invert(cropToFrameTransform);
  }

  @Override
  protected void processImage() {

      FloatingActionButton ButtonFindRecipes = findViewById(R.id.ButtonFindRecipes);

      /*
      if (results != null && results.size() >= 1) {
          Classifier.Recognition recognition = results.get(0);
          if (recognition != null) {
              if (recognition.getTitle() != null){
                  String ingredient = recognition.getTitle();
                  Log.d("ingredient",ingredient);

              }
          }
      }
      */
      ButtonFindRecipes.setOnClickListener(view -> {
          Intent intent = new Intent(this, SuggestedRecipesActivity.class);
          intent.putStringArrayListExtra("ingredients", ingredients);
          Log.v("INGREDIENTS DETECTED",ingredients.toString());
          startActivity(intent);
      });






      if (classifier == null) {
      return;
    }
    rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
    final Canvas canvas = new Canvas(croppedBitmap);
    canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);

    runInBackground(
        new Runnable() {
          @Override
          public void run() {
            final long startTime = SystemClock.uptimeMillis();


            //final List<Classifier.Recognition> results = classifier.recognizeImage(croppedBitmap);
            results = classifier.recognizeImage(croppedBitmap);

            lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
            LOGGER.i("Detect: %s", results);
            cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);

            runOnUiThread(
                new Runnable() {
                  @Override
                  public void run() {
                    Log.v("RESULTS", String.valueOf(results));
                    ingredients = showResultsInBottomSheetStatic(results);
                    /*
                    showResultsInBottomSheet(results);
                    showFrameInfo(previewWidth + "x" + previewHeight);
                    showCropInfo(cropCopyBitmap.getWidth() + "x" + cropCopyBitmap.getHeight());
                    showCameraResolution(canvas.getWidth() + "x" + canvas.getHeight());
                    showRotationInfo(String.valueOf(sensorOrientation));
                    showInference(lastProcessingTimeMs + "ms");
                    */
                  }
                });

            readyForNextImage();
          }
        });
  }

  @Override
  protected void setUseNNAPI(final boolean isChecked) {
    if (classifier == null) {
      return;
    }
    runInBackground(
        () -> {
          if (isChecked) {
            classifier.useNNAPI();
          } else {
            classifier.useCPU();
          }
        });
  }

  @Override
  protected void setNumThreads(final int numThreads) {
    if (classifier == null) {
      return;
    }
    runInBackground(() -> classifier.setNumThreads(numThreads));
  }
}
