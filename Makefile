FRONTEND := ./Frontend/crypto-price
BACKEND := ./Backend/exchange
RESOURCES := $(BACKEND)/src/main/resources


build:
	cd $(FRONTEND) && npm install && npm run-script build
	cp -rv $(FRONTEND)/build/* $(RESOURCES)/public/

run:
	cd $(BACKEND) && ./gradlew :bootRun

clean:
	cd $(FRONTEND) && rm -rf ./node_modules && rm -rf ./build

veryclean: clean
	rm -rf $(RESOURCES)/public/*

