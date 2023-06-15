console.log("starting mongo user creation")

dbName = process.env.MONGO_INITDB_DATABASE
user = process.env.MONGO_NON_ROOT_USERNAME
password = process.env.MONGO_NON_ROOT_PASSWORD

db = db.getSiblingDB(dbName)
db.createUser(
    {
        user: user,
        pwd: password,
        roles: [{ role: "readWrite", db: dbName }],
    },
);

console.log("finished mongo user creation")