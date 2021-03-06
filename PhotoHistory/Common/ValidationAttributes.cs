﻿using System.ComponentModel.DataAnnotations;
using PhotoHistory.Data;
using System;
using System.Globalization;
using PhotoHistory.Models;
using System.Web;

namespace PhotoHistory
{
	public class UniqueUsernameAttribute : ValidationAttribute
	{
		public override bool IsValid(object value)
		{
			UserRepository userRepository = new UserRepository();
			return userRepository.GetByUsername( value as string ) == null;
		}
	}

	public class UniqueEmailAttribute : ValidationAttribute
	{
		public override bool IsValid(object value)
		{
			UserRepository userRepository = new UserRepository();
			return userRepository.GetByEmail( value as string ) == null;
		}
	}

	public class ValidateEmailAttribute : RegularExpressionAttribute
	{
		public ValidateEmailAttribute() :
			base( @"^(([A-Za-z0-9]+_+)|([A-Za-z0-9]+\-+)|([A-Za-z0-9]+\.+)|([A-Za-z0-9]+\++))*[A-Za-z0-9]+@((\w+\-+)|(\w+\.))*\w{1,63}\.[a-zA-Z]{2,6}$" ) { }
	}

    public class ValidUpload : ValidationAttribute
    {
    //    public string SelectName { get; set; }
     //   public string SelectValue { get; set; }
    //    public string RequiredField { get; set; }

        public ValidUpload(): base()
        {
         //   SelectName = selectName;
        //    SelectValue = selectValue;
        //    RequiredField = requiredField;
        }

        protected override ValidationResult IsValid(object value, ValidationContext validationContext)
        {
            var containerType = validationContext.ObjectInstance.GetType();
            var select = containerType.GetProperty("Source");

            if (select != null && (string)select.GetValue(validationContext.ObjectInstance, null) == "remote" )
            {
                var field = containerType.GetProperty("PhotoURL");
                if ( field!=null && string.IsNullOrEmpty( (string) field.GetValue(validationContext.ObjectInstance, null) ) )
                    return new ValidationResult("Enter URL of your photo");
            } 
            else
                if(select != null && (string)select.GetValue(validationContext.ObjectInstance, null) == "local")
                {
                    var field = containerType.GetProperty("FileInput");
                    if (field != null && field.GetValue(validationContext.ObjectInstance, null) ==null)
                        return new ValidationResult("Select file for upload.");
                }
            return ValidationResult.Success;
        }
    }

    public class ValidPhotoDate : ValidationAttribute
    {
        public override bool IsValid(object value)
        {
            if (value != null)
            {
                try
                {
                    DateTime date = DateTime.Parse((string)value);
                    if (date > DateTime.Today)
                        return false;
                    return true;
                }
                catch (Exception)
                {
                    return false;
                }
            }
            // jesli data nie zostala podana to uznajemy ja za poprawna - jest opcjonalna
            return true;
        }
    }

	public class DateYearInRangeAttribute : DataTypeAttribute
	{
		private int _min;
		private int _max;

		public DateYearInRangeAttribute(int min, int max) : base( DataType.Date ) 
		{
			_min = min;
			_max = max;
		}

		public override bool IsValid(object value)
		{
			if ( value != null )
			{
				DateTime date = (DateTime)value;
				return (date != null && date.Year >= _min && date.Year <= _max);
			}

			// jesli data nie zostala podana to uznajemy ja za poprawna - jest opcjonalna
			return true;
		}
	}

	public class MatchCurrentPasswordAttribute : ValidationAttribute
	{
		public override bool IsValid(object value)
		{
			if ( value != null )
			{
				string password = (string)value;

				UserRepository userRepository = new UserRepository();
				UserModel loggedUser = userRepository.GetByUsername( HttpContext.Current.User.Identity.Name );
				if ( loggedUser == null )
					throw new Exception( "Logged user entry couldn't be found" );

				return loggedUser.Password == password.HashMD5();
			}

			return false;
		}
	}

	public class ValidUsernameOrEmailAttribute : ValidationAttribute
	{
		public override bool IsValid(object value)
		{
			if ( value != null )
			{
				string account = (string)value;
				UserRepository users = new UserRepository();
				UserModel user = (users.GetByUsername( account ) ?? users.GetByEmail( account ));
				return user != null/* && user.ActivationCode == null*/;
			}

			return false;
		}
	}

}

