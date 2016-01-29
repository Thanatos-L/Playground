module.exports = function(mongoose) {
  var crypto = require('crypto');// include encryption module
  var AccountSchema = new mongoose.Schema({// define the account data structure
    email:     { type: String, unique: true },
    password:  { type: String},
    phone:     { type: String},
    name:       {type: String},
    photoUrl:  { type: String},
    address: {type: String}
  });

  var Account = mongoose.model('Account', AccountSchema);
    var registerCallback = function(err) {
    if (err) {
      return console.log(err);
    };

    return console.log('Account was created');
  };
  //create an account
  var register = function(email, password, phone, name, res) {
    var shaSum = crypto.createHash('sha256');
    shaSum.update(password);//encrypt password before stored in database

    console.log('Registering ' + email);
    var user = new Account({
      email: email,
      password: shaSum.digest('hex'),
      phone: phone,
      name: name
    });
    user.save(registerCallback);
    res.send(200);
    console.log('Save command was sent');
  }  
  
  //export methods
    return {
    register: register,
    Account: Account
  }
}