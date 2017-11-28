source('../../../programs/bmMethods.R')
# ------------------------------------------------------------------
 
args <- commandArgs(trailingOnly = TRUE)

if (length(args)==0) {
  stop("Please, DIR_PATH of csv files must be supplied", call.=FALSE)
} else if (length(args)==3) {
  # default output file
  DIR_PATH  = args[1] 
  DIR_DATA1 = args[2] 
  DIR_DATA2 = args[3] 
}

iteration <- c(DIR_DATA1,DIR_DATA2)

train <- read.data(DIR_PATH = DIR_PATH, iteration = iteration, file = "train.csv")
train$class <- as.factor(train$class)
train$stats_speed_mean <- NULL
train$stats_speed_sd <- NULL
train$stats_acceleration_mean <- NULL
train$stats_acceleration_sd <- NULL

test <- read.data(DIR_PATH = DIR_PATH, iteration = iteration, file = "test.csv")
test$class <- as.factor(test$class)
test$stats_speed_mean <- NULL
test$stats_speed_sd <- NULL
test$stats_acceleration_mean <- NULL
test$stats_acceleration_sd <- NULL

alg <- c("randomForest")

models <- models.eval(train, test, alg = alg, per.class = T)

NEW.DIR <- paste0(DIR_PATH,"/",gsub("/","",DIR_DATA1),"-",gsub("/","",DIR_DATA2))
dir.create(NEW.DIR)
write.table(x = models[,-c("model","incorrect.instances")], file = paste0(NEW.DIR,"/models.csv"), row.names = FALSE, sep="\t" )
