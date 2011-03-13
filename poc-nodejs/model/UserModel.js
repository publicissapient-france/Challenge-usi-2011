
Model.define('User',{

   collection : 'test_user', // (optional) if not present uses the model name instead.

   // defines your data structure
   types: {
     _id : Object, // if not defined, Mongoose automatically defines for you.
     mail: String,
     firstname : String,
     lastname : String,
     password: String
   },

   indexes : [
     'mail',
//     'bio.age',
//     [['first'],['last']] // compound key indexes
   ],

   static : {}, // adds methods onto the Model.
   methods : {} // adds methods to Model instances.

//   setters: { // custom setters
//     first: function(v){
//       return v.toUpperCase();
//     }
//   },

//   getters: { // custom getters
//     username: function(v){
//       return v.toUpperCase();
//     },
//
//     legalDrinkingAge : function(){
//       return (this.bio.age >= 21) ? true : false;
//     },
//
//     first_last : function(){ // custom getter that merges two getters together.
//       return this.first + ' ' + this.last;
//     }
//   }
 });