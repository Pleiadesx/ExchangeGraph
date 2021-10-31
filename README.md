
# Installation Instructions

## Requirements:

npm (I used `6.14.9`)
gradle
java
kotlin

optional:
Make (I wrote a script to attempt to automate building but I have not tested it on other machines but skip to Auto if interested)

# How to build

First we need to build the frontend which we will place into the spring
server so it can serve the static files directly

## Frontend

Navigate to the frontend folder, install dependencies, and build the app
```bash
cd ./FrontEnd/crypto-price
npm install
npm run-script build
```

After this navigate to the build output directory and copy its contents to the backend directory in the resources directory

```bash
cp -rv ./Frontend/crypto-price/build/* ./Backend/exchange/src/main/resources/public/
```

## Backend

In order to start backend gradle is required. Simply run the wrapper with the task `:bootRun`

```bash
cd ./Backend/exchange
./gradlew :bootRun
```

After that the server should have started in localhost port 8080 by default

## Auto

Simply run the make directives `build` followed by `run`

```bash
make build
make run
```

## Final Note

All that being said I did leave the resource files on this repo just in case there are issues building the frontend so in theory only `make run` or `./gradlew :bootRun` should be necessary.

# Questions


### 1. Are there any sub-optimal choices( or short cuts taken due to limited time ) in your implementation

Yeah, I would say several.

I like working with react-redux beacause it allows me to lift out the state from components but it does require a lot of boilerplate and peculiar situations when dealing with async tasks. So there are a lot of instances where components depend on each other more than they should in my attempt to expedite the process.


I had to leave out rigrous testing because I only had this weekend to work on it due to some other responsibilities so it might not hold up to thorough QA.


### 2. Is any part of it over-designed? ( It is fine to over-design to showcase your skills as long as you are clear about it)

I would say some of the features of the backend were overdesigned (mostly as a response to question 3) and many of these were really to increase reusability.

For example while the current rendition of the website only displays BTC and ETH, on CoinBase and Gemini, it can reasonably extend this `2 x 2` pattern to `n x n` because I wanted to maximize code reuse (for example L2 book-keeping, data caching, Exchange listings, Market listings) for patterns that seem to come up again and again. Granted some bugs might need to be ironed out.


### 3. If you have to scale your solution to 100 users/second traffic what changes would you make, if any?

Actually because the website itself does not deal with real-time trading (i.e. it does not actually trade for you, it simply suggests where one should go) I thought I could leverage the fact that this has some inherent latency due to the user in order to account for higher traffic.


Right now the front-end uses cached forms of the L2 Books for each exchange/market pair. These caches evaluate the price of a given set of Coin Amounts (right now only 1 coin is checked by default but it may be extended easily) every second (this too may be easily altered) using a navigableTreeMap that stores bids and asks.

This task is usally `O(n*log(n))` in a tree map where `n` is the amount of bids and asks. This is usually pretty fast but over `100` users/second it could become a little slower. So I leveraged the formerly mentioned inherent latency and assumed a cached value that is updated every second would look roughly the same to the website end-user.


The cached access has a fresh period after which it will default to the calculation but this allows for much faster serving so I dont think 100 users/second would be too much. Maybe to cut down on bandwidth we could alter the JSON Object keys delivering price data to smaller less meaningful names. (Instead of `oneCoinBuyPrice` maybe `ocbp`)

### 4. What are some other enhancements you would have made, if you had more time to do this implementation


As I said previously I designed certain parts in such a way as to allow extending the information the website can provide.

If I had more time some of the faster, more achievable goals would be to provide the user with more coinValues that they may check (for example due to bids and asks of a given market the price for buying 5 coins at that moment is usually more than 5 * (1 coin price)).

Extending the amount of markets within the currently supported exchanges (Coinbase and Gemini) would also be trivial as they give L2 data for all of their markets and my backend handles that.

Having some kind of history stored server side would also be useful. I have it set up and ready to run using h2 (see pirce_schema.sql in resources folder in the backend) but I disconnected it since, for now at least it wasn't really necessary for the mvp and it wasnt useful in the frontend.

Extending the amount of exchanges would be a little more involved as that would mean familiarizing and adapting another set of Api's.

On the more wishful end one cold forsee allowing a more sophisticated backend engine to handle certain transaction like a higher-level matching engine for multiple exchanges. Although the latency requirements would mean that a more realistic goal would be to set up things like stop-losses rather than deal with real-world industry level trading.