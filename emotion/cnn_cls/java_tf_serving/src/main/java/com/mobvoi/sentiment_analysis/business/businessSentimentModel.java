// Copyright (c) 2016 Mobvoi Inc. All Rights Reserved.
package com.mobvoi.sentiment_analysis.business;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Session.Runner;
import org.tensorflow.Tensor;

import com.mobvoi.sentiment_analysis.common.FeaturesMapper;
import com.mobvoi.sentiment_analysis.servlet.SentimentAnalysisServlet;

/**
 * sentiment analysis using text cnn model
 * 
 * @author wbzhu <wbzhu@mobvoi.com>
 * @Date 12 13, 2018
 */
public class businessSentimentModel {
  
  public static final String MODEL_NAME = "business";
  
  private static final Logger logger = Logger.getLogger(businessSentimentModel.class.getName());

  private static final String MODEL_PATH = SentimentAnalysisServlet.WEB_PATH
          + "WEB-INF/classes/export/business_model/model";
  
  private Session sess;

  private static businessSentimentModel model;
  static {
    model = new businessSentimentModel();
  }

  private businessSentimentModel() {
  }

  public static businessSentimentModel getInstance() {
    return model;
  }

  public void init() {
    logger.info("load business sentiment classification model");
    SavedModelBundle s = SavedModelBundle.load(MODEL_PATH,
            "training_model");
    sess = s.session();
  }

  public JSONObject classicify(String sent, List<String> words) {
    Tensor input_x;
    Tensor input_sentiment;
    Tensor input_regex;
    Tensor input_negation;
    List<Tensor> result = null;
    try {
      logger.info("feature : word ids");
      input_x = Tensor.create(FeaturesMapper.getInstance().mapWordIds(words));
      logger.info("feature : sentiment word ids");
      input_sentiment = Tensor.create(FeaturesMapper.getInstance().mapSentimentWordIds(words));
      logger.info("feature : regex ids");
      input_regex = Tensor.create(FeaturesMapper.getInstance().mapRegexIds(sent));
      logger.info("feature : negation ids");
      input_negation = Tensor.create(FeaturesMapper.getInstance().mapNegationIds(sent));
    } catch (Exception e) {
      logger.error("deal features failed", e);
      return null;
    }

    String label = null;
    float labelProb = 0;
    try {
      Runner runner = sess.runner().feed("input_x:0", input_x);
      runner = runner.feed("sentiment:0", input_sentiment);
      runner = runner.feed("patterns:0", input_regex);
      runner = runner.feed("negs:0", input_negation);
      runner = runner.feed("dropout_keep_prob", Tensor.create(new Float(1)));
      
      runner = runner.fetch("output/predictions:0");
      runner = runner.fetch("output/logits:0");
      result = runner.run();
      
      // predicted label
      Tensor tensor = result.get(0);
      long[] predicted = tensor.copyTo(new long[1]);
      long labelIdx = predicted[0];
      label = FeaturesMapper.getInstance().mapLabel(labelIdx);
      
      // label prob
      tensor = result.get(1);
      float[][] probs = tensor.copyTo(new float[1][FeaturesMapper.getInstance().configParams.get("num_class")]);
      labelProb = probs[0][(int) labelIdx];
    } catch (Exception e) {
      logger.error("run model failed", e);
      return null;
    } finally {
      input_x.close();
      input_sentiment.close();
      input_regex.close();
      input_negation.close();
      for (Tensor t : result) {
        t.close();
      }
    }
    
    JSONObject jsResult = new JSONObject();
    jsResult.put("label", label);
    jsResult.put("score", labelProb);
    return jsResult;
  }

}
