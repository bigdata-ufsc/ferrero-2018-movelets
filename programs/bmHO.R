#source("D:/Users/andres/acmsac2018/programs/bmMethods.R")
source('../../../programs/bmMethods.R')

args <- commandArgs(trailingOnly = TRUE)
if (length(args)==0) {
  stop("Please, DIR_PATH of csv files must be supplied", call.=FALSE)
} else if (length(args)==1) {
  # default output file
  DIR_PATH = args[1] 
}

train.file = paste0(DIR_PATH,"train.csv")
train <- read.csv(train.file)
train$class <- as.factor(train$class)

test.file = paste0(DIR_PATH,"test.csv")
test <- read.csv(test.file)
test$class <- as.factor(test$class)

alg <- c("weka.smo","weka.J48","weka.Bayes")

models <- models.eval(train, test, alg = alg)
write.table(x = models[,-c("model","incorrect.instances")], file = paste0(DIR_PATH,"/models.csv"), row.names = FALSE, sep="\t" )


