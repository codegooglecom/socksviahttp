object AboutForm: TAboutForm
  Left = 470
  Top = 654
  BorderIcons = [biSystemMenu]
  BorderStyle = bsSingle
  Caption = 'About'
  ClientHeight = 182
  ClientWidth = 494
  Color = clBtnFace
  Font.Charset = DEFAULT_CHARSET
  Font.Color = clWindowText
  Font.Height = -11
  Font.Name = 'MS Sans Serif'
  Font.Style = []
  OldCreateOrder = False
  Position = poScreenCenter
  PixelsPerInch = 96
  TextHeight = 13
  object AboutMemo: TMemo
    Left = 8
    Top = 8
    Width = 480
    Height = 137
    Alignment = taCenter
    Enabled = False
    Lines.Strings = (
      'Copyright 2001-2002 Florent CUETO, Sebastien LEBRETON'
      'e-mail : socksviahttp@cqs.dyndns.org'
      'Web page : http://cqs.dyndns.org'
      'Web page of socks via HTTP : http://cqs.dyndns.org/socks'
      ''
      'This product includes software developed by Jonathan Revusky:'
      'JNI Wrapper for Delphi v2.7'
      
        'Copyright (c) 1998-2001 Jonathan Revusky http://www.revusky.com/' +
        ' '
      'All rights reserved.')
    ReadOnly = True
    TabOrder = 0
  end
  object Ok: TButton
    Left = 414
    Top = 152
    Width = 75
    Height = 25
    Caption = 'Ok'
    ModalResult = 1
    TabOrder = 1
    OnClick = OkClick
  end
end
