require(data.table)
# -----------------------------------------------------------------------
# -----------------------------------------------------------------------
 
error.fun.wF <- function (true, predicted){
  1 - fmeasure(predicted,true)
}
  
fmeasure <- function (predicted, true){
  
  levels <- levels(true)
  predicted <- factor(predicted,levels)
  cm = as.matrix(table(Actual = true, Predicted = predicted)) # create the confusion matrix
  diag = diag(cm) # number of correctly classified instances per class 
  rowsums = apply(cm, 1, sum) # number of instances per class
  colsums = apply(cm, 2, sum) # number of predictions per class
  
  precision = ifelse(colsums == 0, 0, diag / colsums)
  recall =    ifelse(rowsums == 0, 0, diag / rowsums)
  
  f1 = 2 * precision * recall / (precision + recall)
  f1 <- replace(f1,is.na(f1),0)
  
  sum(f1 * table(true) / length(true), na.rm = T)
}

fmeasure.weka <- function( evaluation, true ){
  
  f1 <- evaluation$detailsClass[,"fMeasure"]

  sum(f1 * table(true) / length(true), na.rm = T)
  
}

# -------------------------------------------------------------
# BUILDING MODELS
# -------------------------------------------------------------

randomForest.eval <- function (dt){
  
  require(e1071)
  require(randomForest)
  set.seed(1)
  
  tunecontrol <- tune.control(sampling = "cross", cross = 5)  
  
  rf.tunned <- tune(randomForest, class ~ . , data = dt,  
                    proximity=TRUE, ntree = 500,
                    tunecontrol = tunecontrol)
  
  best.model <- rf.tunned$best.model
  best.performance <- rf.tunned$best.performance
  
  data.table( algorithm = "randomForest",
              fmeasure =  fmeasure(best.model$predicted, dt$class),
              Accuracy = 1-best.performance,
              model = list(best.model),
              incorrect.instances = list(instances = which( dt$class != best.model$predicted ))
  )
}

randomForest.eval.x <- function (dt){
  
  require(e1071)
  require(randomForest)
  set.seed(1)
  
  tunecontrol <- tune.control(sampling = "cross", cross = 5)  
  
  rf.tunned <- tune(randomForest, class ~ . , data = dt,  
                    proximity=TRUE, ntree = 200, mtry = 19,
                    tunecontrol = tunecontrol)
  
  best.model <- rf.tunned$best.model
  best.performance <- rf.tunned$best.performance
  
  data.table( algorithm = "randomForest",
              fmeasure =  fmeasure(best.model$predicted, dt$class),
              Accuracy = 1-best.performance,
              model = list(best.model),
              incorrect.instances = list(instances = which( dt$class != best.model$predicted ))
  )
}



weka.smo.eval <- function (dt){
  
  require(RWeka)
  require(rJava)
  
  SMO <- make_Weka_classifier("weka/classifiers/functions/SMO")
  
  model <- SMO (class ~ ., data = dt)
 
  e <- evaluate_Weka_classifier(model, numFolds = 5, seed = 1, class = T)

  data.table( algorithm = "weka.SMO",
              fmeasure =  fmeasure.weka(e, dt$class),
              Accuracy = e$details["pctCorrect"] / 100,
              model = list(model),
              incorrect.instances = list(instances = NA)
  )
}

# --------------------------------------------------------------------

weka.J48.eval <- function (dt){
  
  require(RWeka)
  require(rJava)
  
  J48 <- make_Weka_classifier("weka/classifiers/trees/J48")
  
  model <- J48 (class ~ ., data = dt)
  
  e <- evaluate_Weka_classifier(model, numFolds = 5, seed = 1, class = T)
  
  data.table( algorithm = "weka.J48",
              fmeasure =  fmeasure.weka(e, dt$class),
              Accuracy = e$details["pctCorrect"] / 100,
              model = list(model),
              incorrect.instances = list(instances = NA)
  )
}

# --------------------------------------------------------------------

weka.Bayes.eval <- function (dt){
  
  require(RWeka)
  require(rJava)
  
  NB <- make_Weka_classifier("weka/classifiers/bayes/NaiveBayes")
  
  model <- NB (class ~ ., data = dt)
  
  e <- evaluate_Weka_classifier(model, numFolds = 5, seed = 1, class = T)
  
  data.table( algorithm = "weka.Bayes",
              fmeasure =  fmeasure.weka(e, dt$class),
              Accuracy = e$details["pctCorrect"] / 100,
              model = list(model),
              incorrect.instances = list(instances = NA)
  )
}

# --------------------------------------------------------------------

getCaretResult <- function( fit = NULL, name = NULL){
  
  if ( is.null(fit) ) return()
  
  truth <- fit$pred$obs
  
  predicted <- fit$pred$pred

  cm <- confusionMatrix( data = predicted, reference = truth, mode = "prec_recall" )
  
  data.table( algorithm = ifelse(is.null(name), fit$modelInfo$label, name),
              fmeasure =  fmeasure( predicted, truth),
              Accuracy = cm$overall[['Accuracy']],
              model = list(fit$finalModel),
              incorrect.instances = list(which( predicted != truth ))
  )
  
}

# --------------------------------------------------------------------
# RANDOM FOREST
# --------------------------------------------------------------------
caret.rf.eval <- function (dt, fitcontrol){
  require(caret)
  set.seed(1)
  mtry <- sqrt(ncol(dt)-1)
  tunegrid <- expand.grid(.mtry=mtry)
  fit <- train(class ~ ., data = dt, 
               method = 'rf', 
               trControl = fitcontrol,
               tuneGrid=tunegrid,
               tuneLength=10,
               proximity=TRUE,
               verbose = FALSE)

  getCaretResult(fit,"caret.rf")
}
# -------------------------------------------------------------------

# --------------------------------------------------------------------
# SVM
# --------------------------------------------------------------------
caret.svm.eval <- function (dt, fitcontrol){
  require(caret)
  set.seed(1)
  svmGrid <- expand.grid(sigma= 2^c(-25, -20, -15,-10, -5, 0), C= 2^c(0:5))
  fit <- train(class ~ ., data = dt, 
               method = 'svmRadial', 
               tuneGrid = svmGrid,
               trControl = fitcontrol,
               proximity=TRUE,
               verbose = FALSE)
  getCaretResult(fit,"caret.svm")
}
# -----------------------------------------------------------------

svm.eval <- function (dt){
  require(e1071)
  
  set.seed(1)
  tunecontrol <- tune.control(sampling = "cross", cross = 5, error.fun = error.fun.wF)
  ranges <- list(ranges = list(kernel = c("radial","linear"), epsilon = seq(0,1,0.1), cost = 2^(1:10) ) )
  tunned <- tune(svm, class ~ . , data = dt, proximity=TRUE, tunecontrol = tunecontrol, ranges = ranges)
  best.model <- tunned$best.model
  wF <- 1 - tunned$best.performance
 
  data.table( algorithm = "svm",
              fmeasure =  wF,
              Accuracy = NA,
              model = list(best.model),
              incorrect.instances = NA)
  
}

# --------------------------------------------------------------------


zero.eval <- function (dt){
  require(RWeka)
  truth <- dt$class
  
  n <- length(truth)
  predicted <- rep(names(which.max(table(truth))), n)
  predicted <- factor(predicted, levels(truth))
  
  dt[,-ncol(dt)] <- 0
  
  model <- J48(class ~., dt)
  
  cm <- confusionMatrix( data = predicted, reference = truth, mode = "prec_recall" )

  data.table( algorithm = "zero",
              fmeasure =  fmeasure(predicted, truth),
              Accuracy = cm$overall[['Accuracy']],
              model = list(model),
              incorrect.instances = list(which( truth != predicted ))
  )
}


# --------------------------------------------------------------------

models.eval <- function(tr = NULL, te = NULL, alg=NULL, per.class =F){
  
  require(doParallel)
  require(caret)
  
  if (is.null(tr) | is.null(alg)){
    warning("Please, provide dataset and a list of algorithms.")
    return()
  }
  
  if (is.null(tr$class)){
    warning("Please, provide class attribute in dataset.")
    return()
  }
  
  fitControl <- trainControl(## 10-fold CV
    method = "repeatedcv",
    number = 5,
    repeats = 10,
    savePredictions = "final",
    allowParallel = TRUE)
  
  fitControl <- trainControl(savePredictions = "final", allowParallel = TRUE)
  
  cl <- makeCluster(detectCores())
  registerDoParallel(cl)
  
  results <- rbindlist(
    lapply(alg,function(x){
      model.eval <- eval(parse(text=paste0(x,".eval")))
      
      if (grepl("caret",x))
        result <- model.eval(tr,fitControl)
      else
        result <- model.eval(tr)
      
      if (!is.null(te)){
        predicted <- predict(result$model[[1]],te)
        truth <- te$class
        cm <- confusionMatrix( data = predicted, reference = truth, mode = "prec_recall" )
        
        if (!per.class)
        result <- data.table(result,
                 fmeasureOnTest =  fmeasure(predicted, truth),
                 AccuracyOnTest = cm$overall[['Accuracy']])
        else 
          result <- data.table(result,
                               class =  gsub( "Class: ", "", row.names(cm$byClass) ),
                               F1 = as.numeric(cm$byClass[,"F1"]) )
        
        }
      result
      })
    )
  
  
  stopCluster(cl)
  results
}
# --------------------------------------------------------------------
read.data <- function(DIR_PATH, iteration = NULL, file = "train.csv"){
  
  train <- NULL
  
  lapply(iteration, function(x){
    if (!is.null(iteration))
      DIR_PATH.i <- paste0(DIR_PATH,"/",x,"/")
    else DIR_PATH.i <- paste0(DIR_PATH)
    train.file = paste0(DIR_PATH.i, file)
    train.i <- read.csv(train.file)
    
    if (is.null(train)) {
      train.i$class <- as.factor(train.i$class)
      train <<- train.i
    }
    else {
      cl.idx <- ncol(train.i)
      train.i <- subset(train.i, select = -c(cl.idx))
      names(train.i) <- paste0(names(train.i),"_",x)
      train <<- cbind(train.i, train)
    }
    NULL
  })
  
  train
}