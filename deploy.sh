./gradlew example:webApp:clean
./gradlew example:webApp:compatBrowserProductionDistribution

rm -rf ./docs
mkdir docs
cp -r ./example/webApp/build/dist/compat/productionExecutable/* docs

git add .
git commit -m "deploy"
git push origin gh-pages