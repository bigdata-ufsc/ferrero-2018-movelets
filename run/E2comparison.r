library(data.table)
library(ggplot2)
library(scmamp)
library(xtable)

DIR_PATH <- "../results/ACMSAC2018/E2/"

result.files <- list.files(DIR_PATH, pattern = "models.csv", recursive = T)

alg <- c("weka.SMO","weka.J48","weka.Bayes","randomForest")

results <- rbindlist(lapply(result.files, function(x) {
  
  x1 <- strsplit(x,"/")[[1]]
  
  x2 <- read.csv(paste0(DIR_PATH,"/",x),sep="\t")

  data.table(dataset = x1[1], method = x1[2], subset(x2, algorithm %in% alg ) )
}))

results$fmeasureOnTest <- round(results$fmeasureOnTest,2)

dt <- results

dt <- subset(results, select = c("algorithm","dataset","method","fmeasureOnTest"))

write.csv(dt,paste0(DIR_PATH,"all.csv"))

dt.wide <- reshape(dt, idvar = c("dataset","algorithm"), timevar = "method", direction = "wide")
colnames(dt.wide) <- gsub(".*\\.","", colnames(dt.wide) )

cat("###################\n")
cat("Performance Results\n")
cat("###################\n")

print(dt.wide[order(algorithm,dataset)])

# ------------------------------

dt.test <- dt.wide[,c("Dodge","Zheng","Xiao","Movelets")]
ranked.results <- t(apply(1-dt.test, 1, rank))

cat("\n")
cat("####################\n")
cat("Statistical Analysis\n")
cat("####################\n")

f.test <- friedmanAlignedRanksTest(dt.test, control = "Movelets")
print(f.test)

f.posttest <- friedmanAlignedRanksPost(dt.test, control = "Movelets")
print(f.posttest)