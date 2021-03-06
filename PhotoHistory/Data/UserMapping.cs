﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using NHibernate.Mapping.ByCode.Conformist;
using PhotoHistory.Models;
using NHibernate.Mapping.ByCode;

namespace PhotoHistory.Data
{
    public class UserMapping : ClassMapping<UserModel>
    {
        public UserMapping()
        {
            Table("Users");
            Id(x => x.Id, map =>
            {
                map.Column("user_id");
                map.Generator(Generators.Sequence, g => g.Params(new { sequence = "users_user_id_seq" }));
            });
            Property(x => x.Login, map =>
            {
                map.Column("login");
                map.Length(255);
                map.NotNullable(true);
            });
            Property(x => x.Password, map =>
            {
                map.Column("password");
                map.Length(255);
                map.NotNullable(true);
            });
            Property(x => x.Email, map =>
            {
                map.Column("email");
                map.Length(255);
                map.NotNullable(true);
            });
            Property(x => x.ActivationCode, map =>
            {
                map.Column("activation_code");
            });
            Property(x => x.DateOfBirth, map =>
            {
                map.Column("date_of_birth");
            });
            Property(x => x.About, map =>
            {
                map.Column("about");
            });
            Property(x => x.NotifyComment, map =>
            {
                map.Column("notify_comment");
                map.NotNullable(true);
            });
            Property(x => x.NotifyPhoto, map =>
            {
                map.Column("notify_photo");
                map.NotNullable(true);
            });
            Property(x => x.NotifySubscription, map =>
            {
                map.Column("notify_subscr");
                map.NotNullable(true);
            });
            Bag(x => x.Albums, map =>
            {
                map.Table("Albums");
                map.Key(c=>c.Column("user_id"));
            }, a=>a.OneToMany());
            
            

        }

    }
}
