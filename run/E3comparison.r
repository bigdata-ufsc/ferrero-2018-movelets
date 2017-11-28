library(data.table)
library(ggplot2)
library(scmamp)
library(xtable)

DIR_PATH <- "../results/ACMSAC2018/E3/"
DIR_PATH <- "D:/Users/andres/acmsac2018/results/ACMSAC2018/E3/"

result.files <- list.files(DIR_PATH, pattern = "models.csv", recursive = T)

alg <- c("randomForest")

results <- rbindlist(lapply(result.files, function(x) {
  
  x1 <- strsplit(x,"/")[[1]]
  
  x2 <- read.csv(paste0(DIR_PATH,"/",x),sep="\t")

  data.table(dataset = x1[1], method = x1[2], subset(x2, algorithm %in% alg ) )
}))

dt <- subset(results, select = c("method","class","F1"))
dt$F1 <- round(results$F1,2)

write.csv(dt,paste0(DIR_PATH,"all.csv"))

dt.wide <- reshape(dt, idvar = c("class"), timevar = "method", direction = "wide")
colnames(dt.wide) <- gsub(".*\\.","", colnames(dt.wide) )

dt.wide1 <- dt.wide[c(6,2,1,3,4,5),c("class","Dodge","Zheng","Xiao","Movelets")]

dt.wide2 <- dt.wide[c(6,2,1,3,4,5),c("class","Dodge","Movelets-Dodge","Zheng","Movelets-Zheng","Xiao","Movelets-Xiao")]

cat("###################\n")
cat("Performance Results\n")
cat("###################\n")

print(dt.wide1)

print(dt.wide2)